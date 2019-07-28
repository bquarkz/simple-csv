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

    private CSVWriter writer;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CSVExporter( CSVExporterBuilder< BEAN > builder )
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
    public CSVWriter to( String outputFilename ) throws IOException
    {
        writer = new CSVWriter( outputFilename, builder.getCharset() );
        if( builder.shouldWriteHeader() )
        {
            String columnDelimiter = builder.getDelimiters().getColumn();
            final String[] headers = builder.getCsvParser().getCSVHeaders();
            writer.writeLine( String.join( columnDelimiter, headers ) );
        }
        return writer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FunctionalInterface
    public interface ResultSetBeanFactory< BEAN >
    {
        BEAN from( ResultSet rs ) throws SQLException;
    }

    public final class CSVWriter implements Closeable
    {
        private final FileOutputStream fileOutputStream;
        private final OutputStreamWriter outputStreamWriter;
        private final BufferedWriter bufferedWriter;

        CSVWriter( String output, Charset charset ) throws FileNotFoundException
        {
            this.fileOutputStream = new FileOutputStream( output );
            this.outputStreamWriter = new OutputStreamWriter( fileOutputStream, charset );
            this.bufferedWriter = new BufferedWriter( outputStreamWriter );
        }

        private void writeLine( String line ) throws IOException
        {
            this.bufferedWriter.write( line );
            this.bufferedWriter.write( builder.getDelimiters().getRow() );
        }

        public void writeFrom( ResultSet rs, ResultSetBeanFactory< BEAN > factory ) throws IOException
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
                throw new CSVWriterException( e );
            }
        }

        public void write( BEAN bean ) throws IOException
        {
            final CSVParser< BEAN > csvParser = builder.getCsvParser();
            final CSVDelimiters delimiters = builder.getDelimiters();
            final String csv = csvParser.toCSV( bean, delimiters );
            writeLine( csv );
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
            outputStreamWriter.close();
            fileOutputStream.close();
        }
    }
}
