/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.transaction.log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.neo4j.helpers.collection.CloseableVisitor;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.io.fs.DefaultFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.KernelHealth;
import org.neo4j.kernel.Recovery;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.transaction.CommittedTransactionRepresentation;
import org.neo4j.kernel.impl.transaction.DeadSimpleTransactionIdStore;
import org.neo4j.kernel.impl.transaction.TransactionRepresentation;
import org.neo4j.kernel.impl.transaction.tracing.LogAppendEvent;
import org.neo4j.kernel.impl.transaction.command.Command;
import org.neo4j.kernel.impl.transaction.log.PhysicalLogFile.Monitor;
import org.neo4j.kernel.impl.transaction.log.entry.LogEntryReaderFactory;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.test.TargetDirectory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.kernel.impl.transaction.log.LogRotation.NO_ROTATION;
import static org.neo4j.kernel.impl.transaction.log.PhysicalLogFile.DEFAULT_NAME;
import static org.neo4j.kernel.impl.util.IdOrderingQueue.BYPASS;
import static org.neo4j.test.TargetDirectory.testDirForTest;

public class PhysicalLogicalTransactionStoreTest
{
    private static final KernelHealth kernelHealth = mock( KernelHealth.class );

    private final FileSystemAbstraction fs = new DefaultFileSystemAbstraction();
    @Rule
    public TargetDirectory.TestDirectory dir = testDirForTest( getClass() );
    private File testDir;

    @Before
    public void setup()
    {
        testDir = dir.graphDbDir();
    }

    @Test
    public void shouldOpenCleanStore() throws Exception
    {
        // GIVEN
        TransactionIdStore transactionIdStore = new DeadSimpleTransactionIdStore( 0l, 0 );
        TransactionMetadataCache positionCache = new TransactionMetadataCache( 10, 1000 );

        LifeSupport life = new LifeSupport();
        PhysicalLogFiles logFiles = new PhysicalLogFiles( testDir, DEFAULT_NAME, fs );
        Monitor monitor = new Monitors().newMonitor( PhysicalLogFile.Monitor.class );
        LogFile logFile = life.add( new PhysicalLogFile( fs, logFiles, 1000,
                transactionIdStore, mock( LogVersionRepository.class ), monitor, positionCache ) );

        life.add( new BatchingTransactionAppender( logFile, NO_ROTATION,
                positionCache, transactionIdStore, BYPASS, kernelHealth ) );

        try
        {
            // WHEN
            life.start();
        }
        finally
        {
            life.shutdown();
        }
    }

    @Test
    public void shouldOpenAndRecoverExistingData() throws Exception
    {
        // GIVEN
        TransactionIdStore transactionIdStore = new DeadSimpleTransactionIdStore( 0l, 0l );
        TransactionMetadataCache positionCache = new TransactionMetadataCache( 10, 100 );
        final byte[] additionalHeader = new byte[]{1, 2, 5};
        final int masterId = 2, authorId = 1;
        final long timeStarted = 12345, latestCommittedTxWhenStarted = 4545, timeCommitted = timeStarted + 10;
        LifeSupport life = new LifeSupport();
        final PhysicalLogFiles logFiles = new PhysicalLogFiles( testDir, DEFAULT_NAME, fs );
        Monitor monitor = new Monitors().newMonitor( PhysicalLogFile.Monitor.class );
        LogFile logFile = life.add( new PhysicalLogFile( fs, logFiles, 1000, transactionIdStore,
                mock( LogVersionRepository.class ), monitor, positionCache ) );

        life.start();
        try
        {
            addATransactionAndRewind(life,  logFile, positionCache, transactionIdStore,
                    additionalHeader, masterId, authorId, timeStarted, latestCommittedTxWhenStarted, timeCommitted );
        }
        finally
        {
            life.shutdown();
        }

        life = new LifeSupport();
        final AtomicInteger recoveredTransactions = new AtomicInteger();
        final LogFileRecoverer recoverer = new LogFileRecoverer(
                new LogEntryReaderFactory().versionable(),
                new CloseableVisitor<CommittedTransactionRepresentation, IOException>()
                {
                    @Override
                    public boolean visit( CommittedTransactionRepresentation committedTx ) throws IOException
                    {
                        TransactionRepresentation transaction = committedTx.getTransactionRepresentation();
                        assertArrayEquals( additionalHeader, transaction.additionalHeader() );
                        assertEquals( masterId, transaction.getMasterId() );
                        assertEquals( authorId, transaction.getAuthorId() );
                        assertEquals( timeStarted, transaction.getTimeStarted() );
                        assertEquals( timeCommitted, transaction.getTimeCommitted() );
                        assertEquals( latestCommittedTxWhenStarted, transaction.getLatestCommittedTxWhenStarted() );
                        recoveredTransactions.incrementAndGet();
                        return false;
                    }

                    @Override
                    public void close() throws IOException
                    {
                        // nothing to do
                    }
                } );
        logFile = life.add( new PhysicalLogFile( fs, logFiles, 1000, transactionIdStore, mock( LogVersionRepository.class ), monitor, positionCache ) );

        TransactionAppender appender = new BatchingTransactionAppender( logFile, NO_ROTATION,
                positionCache, transactionIdStore, BYPASS, kernelHealth );
        life.add( appender );

        life.add(new Recovery(new Recovery.SPI()
        {
            @Override
            public void forceEverything()
            {
            }

            @Override
            public long getCurrentLogVersion()
            {
                return 0;
            }

            @Override
            public Visitor<LogVersionedStoreChannel, IOException> getRecoverer()
            {
                return recoverer;
            }

            @Override
            public LogVersionedStoreChannel getLogFile( long recoveryVersion ) throws IOException
            {
                return PhysicalLogFile.openForVersion( logFiles, fs,recoveryVersion );
            }
        }, mock(Recovery.Monitor.class)));

        // WHEN
        try
        {
            life.start();
        }
        finally
        {
            life.shutdown();
        }

        // THEN
        assertEquals( 1, recoveredTransactions.get() );
    }

    @Test
    public void shouldExtractMetadataFromExistingTransaction() throws Exception
    {
        // GIVEN
        TransactionIdStore transactionIdStore = new DeadSimpleTransactionIdStore( 0l, 0l );
        TransactionMetadataCache positionCache = new TransactionMetadataCache( 10, 100 );
        final byte[] additionalHeader = new byte[]{1, 2, 5};
        final int masterId = 2, authorId = 1;
        final long timeStarted = 12345, latestCommittedTxWhenStarted = 4545, timeCommitted = timeStarted + 10;
        LifeSupport life = new LifeSupport();
        PhysicalLogFiles logFiles = new PhysicalLogFiles( testDir, DEFAULT_NAME, fs );
        Monitor monitor = new Monitors().newMonitor( PhysicalLogFile.Monitor.class );
        LogFile logFile = life.add( new PhysicalLogFile( fs, logFiles, 1000,
                transactionIdStore, mock( LogVersionRepository.class ), monitor,
                positionCache ) );

        life.start();
        try
        {
            addATransactionAndRewind( life, logFile, positionCache, transactionIdStore,
                    additionalHeader, masterId, authorId, timeStarted, latestCommittedTxWhenStarted, timeCommitted );
        }
        finally
        {
            life.shutdown();
        }

        life = new LifeSupport();
        final AtomicInteger recoveredTransactions = new AtomicInteger();
        final LogFileRecoverer recoverer = new LogFileRecoverer(
                new LogEntryReaderFactory().versionable(),
                new CloseableVisitor<CommittedTransactionRepresentation, IOException>()
                {
                    @Override
                    public boolean visit( CommittedTransactionRepresentation committedTx ) throws IOException
                    {
                        TransactionRepresentation transaction = committedTx.getTransactionRepresentation();
                        assertArrayEquals( additionalHeader, transaction.additionalHeader() );
                        assertEquals( masterId, transaction.getMasterId() );
                        assertEquals( authorId, transaction.getAuthorId() );
                        assertEquals( timeStarted, transaction.getTimeStarted() );
                        assertEquals( timeCommitted, transaction.getTimeCommitted() );
                        assertEquals( latestCommittedTxWhenStarted, transaction.getLatestCommittedTxWhenStarted() );
                        recoveredTransactions.incrementAndGet();
                        return false;
                    }

                    @Override
                    public void close() throws IOException
                    {
                        // nothing to do
                    }
                } );
        logFile = life.add( new PhysicalLogFile( fs, logFiles, 1000,
                transactionIdStore, mock( LogVersionRepository.class ), monitor,
                positionCache ) );
        TransactionAppender appender = life.add( new BatchingTransactionAppender( logFile, NO_ROTATION,
                positionCache, transactionIdStore, BYPASS, kernelHealth ) );
        final LogicalTransactionStore store = new PhysicalLogicalTransactionStore( logFile, positionCache, appender );

        // WHEN
        life.start();
        try
        {
            recoverer.visit(PhysicalLogFile.openForVersion( logFiles, fs, 0 ));

            positionCache.clear();

            assertThat( store.getMetadataFor( transactionIdStore.getLastCommittedTransactionId() ).toString(),
                    equalTo(
                            "TransactionMetadata[masterId=-1, authorId=-1, startPosition=LogPosition{logVersion=0, " +
                            "byteOffset=16}, checksum=0]" ) );
        }
        finally
        {
            life.shutdown();
        }
    }

    @Test
    public void shouldThrowNoSuchTransactionExceptionIfMetadataNotFound() throws Exception
    {
        // GIVEN
        LogFile logFile = mock( LogFile.class );
        TransactionMetadataCache cache = new TransactionMetadataCache( 10, 10 );
        TransactionIdStore txIdStore = mock( TransactionIdStore.class );

        LifeSupport life = new LifeSupport();

        TransactionAppender appender = life.add( new BatchingTransactionAppender( logFile, NO_ROTATION,
                cache, txIdStore, BYPASS, kernelHealth ) );
        final LogicalTransactionStore txStore = new PhysicalLogicalTransactionStore( logFile, cache, appender );

        try
        {
            life.start();
            // WHEN
            try
            {
                txStore.getMetadataFor( 10 );
                fail( "Should have thrown" );
            }
            catch ( NoSuchTransactionException e )
            {   // THEN Good
            }
        } finally {
            life.shutdown();
        }
    }

    @Test
    public void shouldThrowNoSuchTransactionExceptionIfLogFileIsMissing() throws Exception
    {
        // GIVEN
        LogFile logFile = mock( LogFile.class );
        TransactionIdStore txIdStore = mock( TransactionIdStore.class );
        // a missing file
        when( logFile.getReader( any( LogPosition.class) ) ).thenThrow( new FileNotFoundException() );
        // Which is nevertheless in the metadata cache
        TransactionMetadataCache cache = new TransactionMetadataCache( 10, 10 );
        cache.cacheTransactionMetadata( 10, new LogPosition( 2, 130 ), 1, 1, 100 );

        LifeSupport life = new LifeSupport();

        TransactionAppender appender = life.add( new BatchingTransactionAppender( logFile, NO_ROTATION,
                cache, txIdStore, BYPASS, kernelHealth ) );
        final LogicalTransactionStore txStore = new PhysicalLogicalTransactionStore( logFile, cache, appender );

        try
        {
            life.start();

            // WHEN
            // we ask for that transaction and forward
            try
            {
                txStore.getTransactions( 10 );
                fail();
            }
            catch ( NoSuchTransactionException e )
            {
                // THEN
                // We don't get a FileNotFoundException but a NoSuchTransactionException instead
            }
        } finally
        {
            life.shutdown();
        }

    }

    private void addATransactionAndRewind( LifeSupport life, LogFile logFile,
                                           TransactionMetadataCache positionCache,
                                           TransactionIdStore transactionIdStore,
                                           byte[] additionalHeader, int masterId, int authorId, long timeStarted,
                                           long latestCommittedTxWhenStarted, long timeCommitted ) throws IOException
    {
        TransactionAppender appender = life.add( new BatchingTransactionAppender( logFile, NO_ROTATION, positionCache,
                transactionIdStore, BYPASS, kernelHealth ) );
        PhysicalTransactionRepresentation transaction =
                new PhysicalTransactionRepresentation( singleCreateNodeCommand() );
        transaction.setHeader( additionalHeader, masterId, authorId, timeStarted, latestCommittedTxWhenStarted,
                timeCommitted, -1 );
        appender.append( transaction, LogAppendEvent.NULL );
    }

    private Collection<Command> singleCreateNodeCommand()
    {
        Collection<Command> commands = new ArrayList<>();
        Command.NodeCommand command = new Command.NodeCommand();

        long id = 0;
        NodeRecord before = new NodeRecord( id );
        NodeRecord after = new NodeRecord( id );
        after.setInUse( true );
        command.init( before, after );

        commands.add( command );
        return commands;
    }
}
