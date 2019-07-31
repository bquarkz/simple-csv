package com.bquarkz.simplecsv;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.bquarkz.simplecsv.CSVUtils.splitOnColumns;

public class CSVImporter< BEAN >
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final CSVImporterBuilder< BEAN > builder;

    private CSVReader csvReader;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CSVImporter( CSVImporterBuilder< BEAN > builder )
    {
        this.builder = builder;
    }

    public CSVReader fromFile( String inputFilename ) throws FileNotFoundException
    {
        csvReader = new CSVReader( inputFilename );
        return csvReader;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class CSVReader implements Closeable
    {
        private final InputStream inputStream;
        private final Reader inputReader;
        private final CSVBufferedReader bufferedReader;

        public CSVReader( String inputFilename ) throws FileNotFoundException
        {
            if( inputFilename == null || inputFilename.isEmpty() )
            {
                throw new IllegalArgumentException( "input filename should not be empty" );
            }

            this.inputStream = new FileInputStream( inputFilename );
            this.inputReader = new InputStreamReader( inputStream, builder.getCharset() );
            this.bufferedReader = new CSVBufferedReader( inputReader, builder.getBufferSize(), builder.getDelimiters(), builder.shouldSkipHeader() );
        }

        public CSVReader( InputStream inputStream )
        {
            if( inputStream == null )
            {
                throw new IllegalArgumentException( "outputWriter should not be null" );
            }

            this.inputStream = null;
            this.inputReader = new InputStreamReader( inputStream, builder.getCharset() );
            this.bufferedReader = new CSVBufferedReader( inputReader, builder.getBufferSize(), builder.getDelimiters(), builder.shouldSkipHeader() );
        }

        public Stream< BEAN > stream()
        {
            final CSVDelimiters delimiters = builder.getDelimiters();
            final CSVParser< BEAN > parser = builder.getCsvParser();
            try
            {
                if( builder.shouldVerifyHeader() )
                {
                    final CSVBufferedReader.Row row = bufferedReader._readNextRow();
                    if( row == null || !row.isHeader() ) return Stream.empty();
                    verifyHeader( delimiters, parser, row );
                }
            }
            catch( IOException e )
            {
                if( builder.shouldNotIgnoreErrors() )
                {
                    throw new ExceptionCSVBeanConfiguration( "problems to read header" );
                }
            }
            return bufferedReader
                    .readAsStream()
                    .map( row -> {
                        try
                        {
                            return parser.toBean( row, delimiters, builder.getFactory(), builder.getMappings() );
                        }
                        catch( ExceptionCSVMapping e )
                        {
                            if( builder.shouldNotIgnoreErrors() )
                            {
                                throw e;
                            }
                            return null;
                        }
                    } )
                    .filter( Objects::nonNull );
        }

        public List< BEAN > readNext( final int batch ) throws IOException
        {
            if( batch <= 0 ) throw new IllegalArgumentException( "batch should be bigger than 0" );

            final CSVDelimiters delimiters = builder.getDelimiters();
            final CSVParser< BEAN > parser = builder.getCsvParser();
            List< BEAN > result = new ArrayList<>( batch );
            for( int i = 0; i < batch; i++ )
            {
                try
                {
                    final CSVBufferedReader.Row row = bufferedReader._readNextRow();
                    if( builder.shouldVerifyHeader() && row.isHeader() )
                    {
                        verifyHeader( delimiters, parser, row );
                    }

                    if( row.isHeader() )
                    {
                        i--;
                    }
                    else
                    {
                        result.add( parser.toBean( row.getRowContent(), delimiters, builder.getFactory(), builder.getMappings() ) );
                    }
                }
                catch( ExceptionCSVMapping e )
                {
                    if( builder.shouldNotIgnoreErrors() )
                    {
                        throw new IllegalArgumentException( "problems with bean mappings", e );
                    }
                }
            }

            return result;
        }

        @Override
        public void close() throws IOException
        {
            bufferedReader.close();
            inputReader.close();
            if( inputStream != null ) inputStream.close();
        }

        private void verifyHeader(
                CSVDelimiters delimiters,
                CSVParser< BEAN > parser,
                CSVBufferedReader.Row row )
        {
            final String[] headers = splitOnColumns( row.getRowContent(), delimiters.getContent(), delimiters.getColumn() );
            if( headers.length != parser.getCSVHeaders().length )
            {
                throw new ExceptionCSVBeanConfiguration( "header doesn't fit" );
            }

            for( int i = 0; i < headers.length; i++ )
            {
                if( !parser.getCSVHeaders()[ i ].equals( headers[ i ] ) )
                {
                    throw new ExceptionCSVBeanConfiguration( "header doesn't fit" );
                }
            }
        }
    }
}
