package com.github.ferstl.spring.jdbc.oracle;

import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.github.ferstl.spring.jdbc.oracle.OraclePreparedStatementGuru.createOraclePS;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link OraclePreparedStatementGuru}.
 */
public class OraclePreparedStatementGuruTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private OraclePreparedStatement ops;


  @Before
  public void before() {
    this.ops = createOraclePS();
  }


  @Test
  public void updateWithCompleteBatches() throws SQLException {
    int sendBatchSize = 5;

    this.ops.setExecuteBatch(sendBatchSize);

    // Execute two complete batches
    executeAndVerifyUpdate(sendBatchSize);
    executeAndVerifyUpdate(sendBatchSize);
  }

  @Test
  public void sendBatch() throws SQLException {
    this.ops.setExecuteBatch(5);

    this.ops.executeUpdate();
    this.ops.executeUpdate();

    assertEquals(2, this.ops.sendBatch());
  }

  @Test
  public void sendEmptyBatch() throws SQLException {
    this.ops.setExecuteBatch(5);

    this.expectedException.expect(IllegalStateException.class);
    this.expectedException.expectMessage("empty");

    this.ops.sendBatch();
  }


  @Test
  public void zeroSendBatchSize() throws SQLException {
    int sendBatchSize = 0;

    this.expectedException.expect(IllegalArgumentException.class);
    this.expectedException.expectMessage(": " + sendBatchSize);

    this.ops.setExecuteBatch(0);
  }

  @Test
  public void intermediateBatchSizeChange() throws SQLException {
    this.ops.setExecuteBatch(2);
    this.ops.executeUpdate();

    this.expectedException.expect(IllegalStateException.class);
    this.expectedException.expectMessage(": 1");

    this.ops.setExecuteBatch(5);
  }


  private void executeAndVerifyUpdate(int sendBatchSize) throws SQLException {
    for (int i = 0; i < sendBatchSize - 1; i++) {
      assertEquals(0, this.ops.executeUpdate());
    }

    assertEquals(sendBatchSize, this.ops.executeUpdate());
  }

}
