package com.bquarkz.simplecsv;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class CSVParserAnnotation< BEAN >
        implements CSVParser< BEAN >
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
    private final String[] headers;
    private final boolean shouldWriteHeader;
    private final List< ColumnDetails > columnDetails;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVParserAnnotation( Class< BEAN > classBean )
    {
        CSVBean bean = classBean.getAnnotation( CSVBean.class );
        if( bean == null )
        {
            throw new CSVBeanConfigurationException( "The annotation: "
                    + CSVBean.class.getName() + " is not present on bean: "
                    + classBean.getSimpleName() );
        }

        columnDetails = Stream
                .of( classBean.getDeclaredFields() )
                .filter( f -> ( f.getModifiers() & ( Modifier.FINAL | Modifier.STATIC ) ) == 0 )
                .filter( f -> f.isAnnotationPresent( CSVColumn.class ) )
                .sorted( Comparator.comparingInt( f -> f.getAnnotation( CSVColumn.class ).column() ) )
                .map( ColumnDetails::new )
                .collect( Collectors.toList() );

        if( columnDetails.isEmpty() )
        {
            throw new CSVBeanConfigurationException( "No fields with annotation: "
                    + CSVColumn.class.getName() + " and the BEAN should not be empty" );
        }

        headers = columnDetails
                .stream()
                .map( this::columnNameMapper )
                .toArray( String[]::new );

        shouldWriteHeader = bean.shouldWriteHeader();
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
    private String columnNameMapper( ColumnDetails cd )
    {
        final String name = cd.field.getAnnotation( CSVColumn.class ).name();
        if( name.trim().isEmpty() )
        {
            return cd.field.getName();
        }
        else
        {
            return name;
        }
    }

    @Override
    public String[] getCSVHeaders()
    {
        return headers;
    }

    @Override
    public String toCSV( BEAN bean, CSVDelimiters delimiters )
    {
        try
        {
            final List< String > contents = new ArrayList<>();
            for( ColumnDetails cd : columnDetails )
            {
                cd.field.setAccessible( true );
                final String content = delimiters.getContent()
                        + cd.factory.apply( cd.field.get( bean ) )
                        + delimiters.getContent();
                contents.add( content );
            }
            return String.join( delimiters.getColumn(), contents.toArray( String[]::new ) );
        }
        catch( IllegalAccessException ignored )
        {
        }

        return null;
    }

    @Override
    public BEAN toBean( String csv, CSVDelimiters delimiters )
    {
        return null;
    }

    @Override
    public boolean shouldWriteHeader()
    {
        return shouldWriteHeader;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static class ColumnDetails
    {
        private final Field field;
        private Function< Object, String > factory;

        public ColumnDetails( Field field )
        {
            this( field, Object::toString );
        }

        public ColumnDetails( Field field, Function< Object, String > factory )
        {
            this.field = field;
            this.factory = factory;
        }
    }
}
