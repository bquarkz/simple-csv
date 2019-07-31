package com.bquarkz.simplecsv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Optional.ofNullable;

public class CSVExporterBuilder< BEAN >
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
    private CSVParser< BEAN > csvParser;
    private CSVDelimiters delimiters;

    private Boolean shouldWriteHeader;
    private Charset charset;
    private boolean ignoringErrors;
    private MappingCSV[] mappings;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVExporterBuilder( CSVParser< BEAN > csvParser )
    {
        this.csvParser = csvParser;
        this.delimiters = new CSVDelimiters();
        this.ignoringErrors = true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVDelimiters getDelimiters()
    {
        return delimiters;
    }

    CSVParser< BEAN > getCsvParser()
    {
        return csvParser;
    }

    boolean shouldWriteHeader()
    {
        return ofNullable( shouldWriteHeader ).orElse( csvParser.getParserDetails().shouldWriteHeader() );
    }

    Charset getCharset()
    {
        return ofNullable( charset ).orElse( StandardCharsets.UTF_8 );
    }

    boolean shouldNotIgnoreErrors()
    {
        return !ignoringErrors;
    }

    MappingCSV[] getMappings()
    {
        return mappings;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CSVExporterBuilder< BEAN > withDelimiters( CSVDelimiters delimiters )
    {
        this.delimiters = delimiters;
        return this;
    }

    public CSVExporterBuilder< BEAN > usingParser( CSVParser< BEAN > csvParser )
    {
        this.csvParser = csvParser;
        return this;
    }

    public CSVExporterBuilder< BEAN > shouldWriteHeader( boolean shouldWriteHeader )
    {
        this.shouldWriteHeader = shouldWriteHeader;
        return this;
    }

    public CSVExporterBuilder< BEAN > usingCharset( Charset charset )
    {
        this.charset = charset;
        return this;
    }

    public CSVExporterBuilder< BEAN > ignoringErrors( boolean ignoringErrors )
    {
        this.ignoringErrors = ignoringErrors;
        return this;
    }

    public CSVExporterBuilder< BEAN > withMappings( MappingCSV... mappings )
    {
        this.mappings = mappings;
        return this;
    }

    public CSVExporter< BEAN > build()
    {
        return new CSVExporter<>( this );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
