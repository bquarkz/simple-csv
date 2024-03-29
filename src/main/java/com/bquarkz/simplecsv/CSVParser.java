package com.bquarkz.simplecsv;

import java.util.function.Supplier;

public interface CSVParser< BEAN >
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Default Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Contracts
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String[] getCSVHeaders();
    String toCSV( BEAN bean, CSVDelimiters delimiters, MappingCSV... mappings ) throws ExceptionCSVMapping;
    BEAN toBean( String csv, CSVDelimiters delimiters, Supplier< BEAN > factory, MappingBean... mappings ) throws ExceptionCSVMapping;
    CSVParserDetails getParserDetails();
}
