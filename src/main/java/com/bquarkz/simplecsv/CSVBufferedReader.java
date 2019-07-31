package com.bquarkz.simplecsv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.Stream;

import static com.bquarkz.simplecsv.CSVUtils.fromPosixStartsWithDelimiter;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class CSVBufferedReader implements Closeable, Iterable< String >
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final Object lock;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final CSVDelimiters delimiters;
    private final int bufferSize;
    private final boolean shouldSkipHeader;

    private boolean isHeader;
    private char[] buffer;
    private Reader inputStream;
    private int readPosix;
    private int writePosix;
    private boolean eof;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVBufferedReader( Reader inputStream, int bufferSize, CSVDelimiters delimiters, boolean shouldSkipHeader )
    {
        this.shouldSkipHeader = shouldSkipHeader;
        this.lock = new Object();
        this.inputStream = inputStream;
        this.delimiters = delimiters;
        this.bufferSize = bufferSize;
        this.buffer = new char[ this.bufferSize ];
        this.readPosix = 0;
        this.writePosix = 0;
        this.eof = false;
        this.isHeader = true;
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
    private void shouldStillOpen() throws IOException
    {
        if( inputStream == null ) throw new IOException( "Stream closed" );
    }

    @Override
    public void close() throws IOException
    {
        synchronized( lock )
        {
            if( inputStream == null ) return;
            try
            {
                inputStream.close();
            }
            finally
            {
                inputStream = null;
                buffer = null;
            }
        }
    }

    @Override
    public Iterator< String > iterator()
    {
        return buildIterator();
    }

    private Iterator< String > buildIterator()
    {
        return new Iterator<String>()
        {
            String nextRow = null;

            @Override
            public boolean hasNext()
            {
                if( nextRow != null )
                {
                    return true;
                }
                else
                {
                    try
                    {
                        nextRow = readNextRow();
                    }
                    catch( IOException e )
                    {
                        return false;
                    }
                    return ( nextRow != null );
                }
            }

            @Override
            public String next()
            {
                if( nextRow != null || hasNext() )
                {
                    String line = nextRow;
                    nextRow = null;
                    return line;
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public Stream< String > readAsStream()
    {
        final Iterator<String> iterator = buildIterator();
        return stream( spliteratorUnknownSize( iterator, Spliterator.ORDERED | Spliterator.NONNULL ), false );
    }

    public String readNextRow() throws IOException
    {
        Row row = _readNextRow();
        if( row == null ) return null;
        if( shouldSkipHeader && row.isHeader )
        {
            return readNextRow();
        }
        else
        {
            return row.getRowContent();
        }
    }

    Row _readNextRow() throws IOException
    {
        // skip line when it finds a comment delimiter on beginning
        boolean skipLine = fromPosixStartsWithDelimiter( buffer, readPosix, delimiters.getComment() );
        boolean eor = false;
        boolean stateContent = false;
        final StringBuffer sb = new StringBuffer();
        do
        {
            if( fromPosixStartsWithDelimiter( buffer, readPosix, delimiters.getContent() ) )
            {
                final char[] nextChars = nextFewChars( delimiters.getContent().length() );
                if( nextChars == null ) break;
                if( !skipLine ) sb.append( nextChars );
                stateContent = !stateContent;
            }
            else
            {
                char[] nextChars = nextFewChars( 1 );
                if( nextChars == null ) break;
                if( !skipLine ) sb.append( nextChars[ 0 ] );
            }

            if( !stateContent )
            {
                if( fromPosixStartsWithDelimiter( buffer, readPosix, delimiters.getRow() ) )
                {
                    final char[] nextChars = nextFewChars( delimiters.getRow().length() );// skip the row delimiter
                    eor = ( nextChars == null ) || !skipLine;
                    skipLine = false;
                }
            }
        } while( !eor || skipLine );
        final String result = sb.toString();
        final Row row = result.isEmpty() ? null : new Row( isHeader, result );
        this.isHeader = false;
        return row;
    }

    private char[] nextFewChars( int nextFewChars ) throws IOException
    {
        final char[] smallBuffer = new char[ nextFewChars ];
        final int size = copyTo( smallBuffer, 0, nextFewChars );
        return size == 0 ? null : smallBuffer;
    }

    private int copyTo( char[] charBuffer, int offset, int length ) throws IOException
    {
        synchronized( lock )
        {
            shouldStillOpen();

            if( ( offset < 0 ) || ( length < 0 ) || ( offset > charBuffer.length )
                    || ( ( offset + length ) > charBuffer.length ) || ( ( offset + length ) < 0 ) )
            {
                throw new IndexOutOfBoundsException();
            }
            else if( length == 0 )
            {
                return 0;
            }

            int totalRead = readChunks( charBuffer, offset, length );
            if( totalRead <= 0 ) return totalRead;
            while( totalRead < length )
            {
                final int currentRead = readChunks( charBuffer, offset + totalRead, length - totalRead );
                if( currentRead <= 0 ) break;
                totalRead += currentRead;
            }
            return totalRead;
        }
    }

    /**
     * Reads characters into a portion of an array, reading from the underlying
     * stream if necessary.
     */
    private int readChunks( char[] chunk, int offset, int length ) throws IOException
    {
        if( writePosix == 0 )
        {
            fillBufferWithFreshBytes();
        }

        // copy remaining buffer to output buffer
        final int delta = ( readPosix + length >= writePosix ) ? writePosix - readPosix : length;
        if( delta == 0 ) return 0;
        System.arraycopy( buffer, readPosix, chunk, offset, delta );
        increaseReadPosixBy( delta );
        if( inputStream.ready() )
        {
            if( readPosix == 0 ) // an entire copy have to be made because, so ...
            {
                fillBufferWithFreshBytes();
            }
        }
        return delta;
    }

    /**
     * Increase readPosix as a circular belt
     */
    private void increaseReadPosixBy( int increasePosition )
    {
        readPosix += increasePosition;
        if( readPosix >= bufferSize )
        {
            readPosix -= bufferSize;
        }
    }

    /**
     * Fills the input buffer with fresh data
     */
    private void fillBufferWithFreshBytes() throws IOException
    {
        synchronized( lock )
        {
            eof = false;

            shouldStillOpen();

            if( writePosix == bufferSize || writePosix == 0 ) // fills a entire buffer on a single step
            {
                int nCharsSingleStep = inputStream.read( buffer, 0, bufferSize );
                if( nCharsSingleStep <= 0 )
                {
                    eof = true;
                }
                else if( nCharsSingleStep < bufferSize )
                {
                    eof = true;
                    writePosix = nCharsSingleStep; // input stream could have less bytes than buffer
                }
                else
                {
                    writePosix = bufferSize;
                }
            }
            else
            {
                int delta = bufferSize - writePosix;
                int nCharsStepOne = inputStream.read( buffer, writePosix, delta );
                if( nCharsStepOne <= 0 )
                {
                    eof = true;
                }
                else if( nCharsStepOne < delta )
                {
                    eof = true;
                    writePosix = nCharsStepOne;
                }
                else
                {
                    int nCharsStepTwo = inputStream.read( buffer, 0, writePosix );
                    if( nCharsStepTwo <= 0 )
                    {
                        eof = true;
                    }
                    else if( nCharsStepTwo < writePosix )
                    {
                        eof = true;
                        writePosix = nCharsStepOne;
                    }
                    else
                    {
                        writePosix = nCharsStepOne + nCharsStepTwo;
                    }
                }
            }
            if( writePosix >= bufferSize ) writePosix = bufferSize;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static class Row
    {
        private final boolean isHeader;
        private final String rowContent;

        Row( boolean isHeader, String rowContent )
        {
            this.isHeader = isHeader;
            this.rowContent = rowContent;
        }

        public boolean isHeader()
        {
            return isHeader;
        }

        public String getRowContent()
        {
            return rowContent;
        }
    }
}
