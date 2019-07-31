package com.bquarkz.simplecsv;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bquarkz.simplecsv.CSVUtils.splitOnColumns;
import static java.util.Optional.ofNullable;
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
    private final CSVParserDetails parserDetails;
    private final List< Field > columnFields;
    private final Map< String, Field > fieldsMap;
    private final CSVAutoMapper autoMapper;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVParserAnnotation( Class< BEAN > classBean )
    {
        this( classBean, new CSVBaseAutoMapper() );
    }

    CSVParserAnnotation( Class< BEAN > classBean, CSVAutoMapper autoMapper )
    {
        this.autoMapper = autoMapper;

        CSVBean bean = classBean.getAnnotation( CSVBean.class );
        if( bean == null )
        {
            throw new ExceptionCSVBeanConfiguration( "The annotation: "
                    + CSVBean.class.getName() + " is not present on bean: "
                    + classBean.getSimpleName() );
        }

        columnFields = Stream
                .of( classBean.getDeclaredFields() )
                .filter( f -> ( f.getModifiers() & ( Modifier.FINAL | Modifier.STATIC ) ) == 0 )
                .filter( f -> f.isAnnotationPresent( CSVColumn.class ) )
                .sorted( Comparator.comparingInt( f -> f.getAnnotation( CSVColumn.class ).column() ) )
                .collect( Collectors.toList() );

        if( columnFields.isEmpty() )
        {
            throw new ExceptionCSVBeanConfiguration( "No fields with annotation: "
                    + CSVColumn.class.getName() + " and "
                    + classBean.getSimpleName() + " should not be empty" );
        }

        headers = columnFields
                .stream()
                .map( this::columnNameMapper )
                .toArray( String[]::new );

        fieldsMap = columnFields
                .stream()
                .collect( Collectors.toMap( this::columnNameMapper, f -> f ) );

        parserDetails = new CSVParserDetails( bean );
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
    private String columnNameMapper( Field field )
    {
        final String name = field.getAnnotation( CSVColumn.class ).name();
        if( name.trim().isEmpty() )
        {
            return field.getName();
        }
        else
        {
            return name;
        }
    }

    @Override
    public CSVParserDetails getParserDetails()
    {
        return parserDetails;
    }

    @Override
    public String[] getCSVHeaders()
    {
        return headers;
    }

    @Override
    public String toCSV(
            final BEAN bean,
            final CSVDelimiters delimiters,
            final MappingCSV... mappings ) throws ExceptionCSVMapping
    {
        final Map< String, MapperCSV > maps = Stream
                .of( mappings )
                .collect( toMap( MappingCSV::columnName, MappingCSV::mapper ) );

        final List< String > contents = new ArrayList<>();
        for( final Field field : columnFields )
        {
            field.setAccessible( true );
            final MapperCSV mapper = ofNullable( maps.get( columnNameMapper( field ) ) ).orElse( Object::toString );
            try
            {
                final Object value = field.get( bean );
                final String mappedValue = value == null ? null : mapper.map( value );
                final String content = CSVUtils.embrace( delimiters.getContent(), mappedValue );
                contents.add( content );
            }
            catch( IllegalArgumentException | IllegalAccessException e )
            {
                throw new ExceptionCSVBeanConfiguration( "bean configuration problem", e );
            }
            catch( Exception e )
            {
                throw new ExceptionCSVMapping( e );
            }
        }
        return String.join( delimiters.getColumn(), contents.toArray( new String[ contents.size() ] ) );
    }

    @Override
    public BEAN toBean(
            final String csv,
            final CSVDelimiters delimiters,
            final Supplier< BEAN > factory,
            final MappingBean... mappings ) throws ExceptionCSVMapping
    {
        final Map< String, MapperBean > maps = Stream
                .of( mappings )
                .collect( toMap( MappingBean::columnName, MappingBean::mapper ) );

        final String[] contents = splitOnColumns( csv, delimiters.getContent(), delimiters.getColumn() );
        if( contents.length != headers.length )
        {
            throw new ExceptionCSVMapping( "number of columns on content doesn't fit header number of columns" );
        }

        final BEAN bean = factory.get();
        for( int i = 0; i < headers.length; i++ )
        {
            final String header = headers[ i ];
            final String content = CSVUtils.desembrace( delimiters.getContent(), contents[ i ] );

            final Field field = fieldsMap.get( header );
            final MapperBean mapper = ofNullable( maps.get( header ) )
                    .orElseGet( () -> csvContent -> autoMapper.map( field.getType(), delimiters.getContent(), csvContent ) );
            try
            {
                final Object object = mapper.map( content );
                field.setAccessible( true );
                field.set( bean, object );
            }
            catch( IllegalAccessException e )
            {
                throw new ExceptionCSVBeanConfiguration( "problems with bean configuration for [ " + header + " ]", e );
            }
            catch( Exception e )
            {
                throw new ExceptionCSVMapping( "factory mapping for [ " + header + " ] does't fit", e );
            }
        }

        return bean;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static class CSVField
    {
        private Class< ? > klass;
        private Field field;
    }
}
