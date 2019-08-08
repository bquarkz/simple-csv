package com.bquarkz.simplecsv;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class CSVImporterBuilder< BEAN >
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final Integer DEFAULT_BUFFER_SIZE = 8192;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Class< BEAN > beanClass;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private CSVParser< BEAN > csvParser;
    private CSVDelimiters delimiters;

    private boolean ignoringErrors;
    private Supplier< BEAN > factory;
    private Charset charset;
    private Boolean shouldSkipHeader;
    private Boolean shouldVerifyHeader;

    private MappingBean[] mappings;
    private int bufferSize;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVImporterBuilder( Class< BEAN > beanClass, CSVParser< BEAN > csvParser )
    {
        this.beanClass = beanClass;
        this.csvParser = csvParser;
        this.ignoringErrors = true;
        this.delimiters = new CSVDelimiters();
        this.factory = getDefaultConstructor( beanClass );
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVParser< BEAN > getCsvParser()
    {
        return csvParser;
    }

    boolean shouldNotIgnoreErrors()
    {
        return !ignoringErrors;
    }

    CSVDelimiters getDelimiters()
    {
        return delimiters;
    }

    Supplier< BEAN > getFactory()
    {
        return factory;
    }

    Charset getCharset()
    {
        return ofNullable( charset ).orElse( StandardCharsets.UTF_8 );
    }

    boolean shouldSkipHeader()
    {
        return ofNullable( shouldSkipHeader ).orElse( csvParser.getParserDetails().shouldSkipHeader() );
    }

    Boolean shouldVerifyHeader()
    {
        return shouldSkipHeader() ? false : ofNullable( shouldVerifyHeader ).orElse( csvParser.getParserDetails().shouldVerifyHeader() );
    }

    MappingBean[] getMappings()
    {
        return mappings;
    }

    int getBufferSize()
    {
        return bufferSize;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Supplier< BEAN > getDefaultConstructor( Class< BEAN > beanClass )
    {
        return () -> {
            try
            {
                return beanClass.getConstructor().newInstance();
            }
            catch( InstantiationException
                    | NoSuchMethodException
                    | InvocationTargetException
                    | IllegalAccessException e )
            {
                throw new ExceptionCSVBeanConfiguration( "bean configuration problem: no public default constructor perhaps?", e );
            }
        };
    }

    public CSVImporterBuilder< BEAN > withDelimiters( CSVDelimiters delimiters )
    {
        this.delimiters = delimiters;
        return this;
    }

    public CSVImporterBuilder< BEAN > withParser( CSVParser< BEAN > csvParser )
    {
        this.csvParser = csvParser;
        return this;
    }

    public CSVImporterBuilder< BEAN > withBeanFactory( Supplier< BEAN > factory )
    {
        this.factory = factory != null ? factory : getDefaultConstructor( beanClass );
        return this;
    }

    public CSVImporterBuilder< BEAN > withCharset( Charset charset )
    {
        this.charset = charset;
        return this;
    }

    public CSVImporterBuilder< BEAN > skippingHeader( boolean skippingHeader )
    {
        this.shouldSkipHeader = skippingHeader;
        return this;
    }

    public CSVImporterBuilder< BEAN > verifyingHeader( boolean verifyingHeader )
    {
        this.shouldVerifyHeader = verifyingHeader;
        return this;
    }

    public CSVImporterBuilder< BEAN > ignoringErrors( boolean ignoringErrors )
    {
        this.ignoringErrors = ignoringErrors;
        return this;
    }

    public CSVImporter< BEAN > build( MappingBean... mappings )
    {
        this.mappings = mappings;
        return new CSVImporter<>( this );
    }

    public CSVImporterBuilder< BEAN > withBufferSize( int bufferSize )
    {
        this.bufferSize = bufferSize;
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
