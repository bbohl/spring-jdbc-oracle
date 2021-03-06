/*
 * Copyright (c) 2013 by Stefan Ferstl <st.ferstl@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.spring.jdbc.oracle;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import oracle.jdbc.OraclePreparedStatement;

import static com.github.ferstl.spring.jdbc.oracle.RowCountPerBatchMatcher.matchesBatchedRowCounts;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * JUnit tests for {@link ParameterizedBatchingPreparedStatementCallback}.
 */
public class ParameterizedBatchingPreparedStatementCallbackTest {

  private OraclePreparedStatement ops;

  @Before
  public void before() {
    this.ops = OracleJdbcGuru.createOraclePS();
  }

  @Test
  public void completeBatches() throws SQLException {
    doInPreparedStatement(3, 6);
  }

  @Test
  public void incompleteBatches() throws SQLException {
    doInPreparedStatement(5, 2);
  }

  @Test
  public void completeAndIncompleteBatches() throws SQLException {
    doInPreparedStatement(3, 8);
  }

  @Test
  public void emptyPss() throws SQLException {
    doInPreparedStatement(3, 0);
  }

  private void doInPreparedStatement(int batchSize, int batchArgSize) throws SQLException {
    @SuppressWarnings("unchecked")
    ParameterizedPreparedStatementSetter<String> ppss = mock(ParameterizedPreparedStatementSetter.class);

    // Create the arguments for the batch update
    ArrayList<String> batchArgs = new ArrayList<>(batchArgSize);
    for (int i = 0; i < batchArgSize; i++) {
      batchArgs.add(Integer.toString(i));
    }

    ParameterizedBatchingPreparedStatementCallback<String> psc =
        new ParameterizedBatchingPreparedStatementCallback<>(ppss, batchSize, batchArgs);
    int[][] result = psc.doInPreparedStatement(this.ops);

    assertThat(result, matchesBatchedRowCounts(batchSize, batchArgSize));
    verifyPreparedStatementCalls(batchArgSize, ppss);
  }


  private void verifyPreparedStatementCalls(int batchArgSize, ParameterizedPreparedStatementSetter<String> ppss)
  throws SQLException {

    for (int i = 0; i < batchArgSize; i++) {
      verify(ppss).setValues(this.ops, Integer.toString(i));
      verify(this.ops, times(batchArgSize)).executeUpdate();
    }
  }

}
