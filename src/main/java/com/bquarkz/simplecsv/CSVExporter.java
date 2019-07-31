package com.bquarkz.simplecsv;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CSVExporter< BEAN >
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
    private final CSVExporterBuilder< BEAN > builder;
    private CSVWriter csvWriter;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVExporter( CSVExporterBuilder< BEAN > builder )
    {
        this.builder = builder;
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
    public CSVWriter toFile( String outputFilename ) throws IOException
    {
        csvWriter = new CSVWriter( outputFilename );
        return csvWriter.writeHeaders();
    }

    public CSVWriter toWriter( OutputStream outputStream ) throws IOException
    {
        csvWriter = new CSVWriter( outputStream );
        return csvWriter.writeHeaders();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FunctionalInterface
    public interface ResultSetBeanFactory< BEAN > extends CSVBeanFactory< ResultSet, BEAN >
    {
        @Override
        BEAN from( ResultSet rs ) throws SQLException;
    }

    @FunctionalInterface
    public interface CSVBeanFactory< INPUT, BEAN >
    {
        BEAN from( INPUT rs ) throws Exception;
    }

    public final class CSVWriter implements Closeable
    {
        private final OutputStream outputStream;
        private final Writer outputWriter;
        private final BufferedWriter bufferedWriter;

        CSVWriter( OutputStream outputStream )
        {
            if( outputStream == null )
            {
                throw new IllegalArgumentException( "output stream should not be null" );
            }

            this.outputStream = outputStream;
            this.outputWriter = new OutputStreamWriter( outputStream, builder.getCharset() );
            this.bufferedWriter = new BufferedWriter( outputWriter );
        }

        CSVWriter( String outputFilename ) throws FileNotFoundException
        {
            if( outputFilename == null || outputFilename.isEmpty() )
            {
                throw new IllegalArgumentException( "output filename should not be empty" );
            }

            this.outputStream = new FileOutputStream( outputFilename );
            this.outputWriter = new OutputStreamWriter( outputStream, builder.getCharset() );
            this.bufferedWriter = new BufferedWriter( outputWriter );
        }

        private CSVWriter writeHeaders() throws IOException
        {
            if( builder.shouldWriteHeader() )
            {
                String columnDelimiter = builder.getDelimiters().getColumn();
                final String[] headers = builder.getCsvParser().getCSVHeaders();
                writeLine( String.join( columnDelimiter, headers ) );
            }
            return this;
        }

        private void writeLine( String line ) throws IOException
        {
            this.bufferedWriter.write( line );
            this.bufferedWriter.write( builder.getDelimiters().getRow() );
        }

        public void writeFrom(
                final ResultSet rs,
                final ResultSetBeanFactory< BEAN > factory ) throws IOException
        {
            try
            {
                while( rs.next() )
                {
                    final BEAN bean = factory.from( rs );
                    write( bean );
                }
            }
            catch( SQLException e )
            {
                throw new ExceptionCSVWriter( e );
            }
        }

        public void write( BEAN bean ) throws IOException
        {
            final CSVParser< BEAN > csvParser = builder.getCsvParser();
            final CSVDelimiters delimiters = builder.getDelimiters();
            try
            {

                final String csv = csvParser.toCSV( bean, delimiters, builder.getMappings() );
                writeLine( csv );
            }
            catch( ExceptionCSVMapping e )
            {
                if( builder.shouldNotIgnoreErrors() )
                {
                    throw new IllegalArgumentException( "problems with bean mappings", e );
                }
            }
        }

        public void write( List< BEAN > beans ) throws IOException
        {
            for( BEAN bean : beans )
            {
                write( bean );
            }
        }

        @Override
        public void close() throws IOException
        {
            bufferedWriter.close();
            outputWriter.close();
            if( outputStream != null )
            {
                outputStream.close();
            }
        }
    }
}
