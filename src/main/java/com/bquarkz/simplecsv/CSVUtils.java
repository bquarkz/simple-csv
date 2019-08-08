package com.bquarkz.simplecsv;

import java.util.ArrayList;
import java.util.List;

public interface CSVUtils
{
    static String embrace( String delimiter, String string )
    {
        if( delimiter == null && string == null ) return null;
        if( delimiter == null ) return string;
        if( string == null ) return delimiter + delimiter;
        return delimiter + string + delimiter;
    }

    static String desembrace( String delimiter, String string )
    {
        if( string == null ) return null;
        if( delimiter == null ) return string;
        final String trimString = string.trim();
        if( trimString.startsWith( delimiter ) && trimString.endsWith( delimiter ) )
        {
            return string.substring( delimiter.length(), string.length() - delimiter.length() );
        }
        else
        {
            return string;
        }
    }

    static boolean fromPosixStartsWithDelimiter( final char[] buffer, final int posix, final String content )
    {
        if( buffer == null ) return false;
        if( content == null || content.isEmpty() ) return true;
        if( buffer.length < content.length() ) return false;

        for( int index = 0; index < content.length(); index++ )
        {
            final int tempPosix = posix + index;
            if( tempPosix >= buffer.length ) return false;
            if( content.charAt( index ) != buffer[ tempPosix ] ) return false;
        }
        return true;
    }

    static String[] splitOnColumns( String content, String contentDelimiter, String columnDelimiter )
    {
        final char[] buffer = content.toCharArray();
        int posix = 0;
        boolean stateContent = false;

        List< String > contents = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        do
        {
            if( fromPosixStartsWithDelimiter( buffer, posix, contentDelimiter ) )
            {
                sb.append( content, posix, posix + contentDelimiter.length() );
                posix += contentDelimiter.length();
                stateContent = !stateContent;
            }
            // for "blank" columns
            else if( !stateContent && fromPosixStartsWithDelimiter( buffer, posix, columnDelimiter ) )
            {
                posix += columnDelimiter.length();
                if( posix < content.length() )
                {
                    contents.add( sb.toString() );
                }
                sb = new StringBuffer();
            }
            else if( posix < content.length() )
            {
                char nextChar = content.charAt( posix );
                posix++;
                sb.append( nextChar );
            }

            if( posix >= content.length() )
            {
                stateContent = false;
            }

            if( !stateContent )
            {
                if( posix >= content.length() )
                {
                    contents.add( sb.toString() );
                }

                // normally for last content
                if( fromPosixStartsWithDelimiter( buffer, posix, columnDelimiter ) )
                {
                    posix += columnDelimiter.length();
                    contents.add( sb.toString() );
                    sb = new StringBuffer();
                }
            }
        } while( posix < content.length() );

        return contents.toArray( new String[ contents.size() ] );
    }

}
