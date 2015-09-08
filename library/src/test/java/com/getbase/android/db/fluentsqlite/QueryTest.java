package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Expressions.sum;
import static com.getbase.android.db.fluentsqlite.Query.select;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import com.getbase.android.db.fluentsqlite.Expressions.CollatingSequence;
import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.getbase.android.db.fluentsqlite.Query.QueryBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;

import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryTest {

  @Mock
  private SQLiteDatabase mDb;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldBuildTheSimpleSelect() throws Exception {
    select().allColumns().from("table_a").build().perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheSimpleDistinctSelect() throws Exception {
    Query.select().distinct().allColumns().from("table_a").build().perform(mDb);

    verify(mDb).rawQuery(eq("SELECT DISTINCT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .union()
        .select().allColumns().from("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionCompoundQueryWithDistinctSelect() throws Exception {
    select().allColumns().from("table_a")
        .union()
        .select()
        .distinct().allColumns().from("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT DISTINCT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionAllCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .union().all()
        .select().allColumns().from("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION ALL SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheExceptCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .except()
        .select().allColumns().from("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a EXCEPT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheIntersectCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .intersect()
        .select().allColumns().from("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a INTERSECT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithSelection() throws Exception {
    select().allColumns().from("table_a")
        .where("column=?", 0)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (column=?)"), eq(new String[] { "0" }));
  }

  @Test
  public void shouldBuildTheQueryWithMultipleSelections() throws Exception {
    select().allColumns().from("table_a")
        .where("column=?", 0)
        .where("other_column=?", 1)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (column=?) AND (other_column=?)"), eq(new String[] { "0", "1" }));
  }

  @Test
  public void shouldBuildTheQueryWithLeftJoin() throws Exception {
    select().allColumns().from("table_a")
        .left().join("table_b")
        .on("column_a=?", 0)
        .where("column_b=?", 1)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LEFT JOIN table_b ON (column_a=?) WHERE (column_b=?)"), eq(new String[] { "0", "1" }));
  }

  @Test
  public void shouldBuildTheQueryWithCrossJoin() throws Exception {
    select().allColumns().from("table_a")
        .cross().join("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a CROSS JOIN table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithNaturalJoin() throws Exception {
    select().allColumns().from("table_a")
        .natural().join("table_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a NATURAL JOIN table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithAliasedJoin() throws Exception {
    select().allColumns().from("table_a")
        .join("table_b").as("b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b AS b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryJoinedWithSubquery() throws Exception {
    select().allColumns().from("table_a")
        .join(
            select().allColumns().from("table_b").build()
        )
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN (SELECT * FROM table_b)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithMultipleInnerJoins() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .join("table_c")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b JOIN table_c"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithMultipleJoins() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .left().join("table_c")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b LEFT JOIN table_c"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryFromSubquery() throws Exception {
    select()
        .allColumns()
        .from(
            select().allColumns().from("table_a").build()
        )
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM (SELECT * FROM table_a)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithJoinUsingColumnList() throws Exception {
    select().allColumns().from("table_a")
        .join("table_b")
        .using("col_b", "col_c")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b USING (col_b, col_c)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithSingleColumnProjection() throws Exception {
    select()
        .column("a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithAliasedColumnProjection() throws Exception {
    select()
        .column("a").as("aaa")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a AS aaa FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithAliasedColumnListProjection() throws Exception {
    select()
        .columns("a", "b", "c").of("table_a").asColumnNames()
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.a AS a, table_a.b AS b, table_a.c AS c FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldConcatenateProjections() throws Exception {
    select()
        .column("a")
        .column("b")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a, b FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryForAllColumnsFromSpecifiedTable() throws Exception {
    select()
        .allColumns().of("table_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.* FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithAliasedTable() throws Exception {
    select()
        .from("table_a").as("a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a AS a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptEmptyProjection() throws Exception {
    select()
        .column("a")
        .columns()
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullProjection() throws Exception {
    select()
        .columns((String[]) null)
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldSelectAllColumnsWhenProjectionIsNotSpecified() throws Exception {
    select()
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullSelection() throws Exception {
    select()
        .from("table_a")
        .where((String) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullExpressionSelection() throws Exception {
    select()
        .from("table_a")
        .where((Expression) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullSortOrder() throws Exception {
    select()
        .from("table_a")
        .orderBy((String) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithNumericLimit() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionLimit() throws Exception {
    select()
        .from("table_a")
        .limit("1+1")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1+1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithNumericLimitOffset() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .offset(1)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1 OFFSET 1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionLimitOffset() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .offset("1+1")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1 OFFSET 1+1"), eq(new String[0]));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldAllowSettingTheLimitOnlyOnce() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .limit(1);
  }

  @Test
  public void shouldBuildQueryWithoutAnyTables() throws Exception {
    select()
        .column("1500")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 1500"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithGroupByClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleGroupByClauses() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .groupBy("col_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a, col_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithGroupByAndHavingClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?)"), eq(new String[] { "1" }));
  }

  @Test
  public void shouldBuildQueryWithGroupByAndMultipleHavingClauses() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .having("col_c=?", 2)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?) AND (col_c=?)"), eq(new String[] { "1", "2" }));
  }

  @Test
  public void shouldIgnoreNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidNumericalOffsetAfterNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .offset(1);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidExpressionOffsetAfterNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .offset("1+1");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowHavingClauseWithoutGroupByClause() throws Exception {
    select()
        .from("table_a")
        .having("col_a=?", 1)
        .build()
        .perform(mDb);
  }

  @Test
  public void shouldBuildQueryWithOrderByWithoutSpecifiedSorting() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithAscSort() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .asc()
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a ASC"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithDescSort() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .desc()
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a DESC"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleOrderByClauses() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .orderBy("col_b")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a, col_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithSpecifiedCollation() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .collate("NOCASE")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a COLLATE NOCASE"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithSpecifiedCollationUsingCollatingSequence() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .collate(CollatingSequence.NOCASE)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a COLLATE NOCASE"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInProjection() throws Exception {
    select()
        .expr(column("col_a"))
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInOrderBy() throws Exception {
    select()
        .from("table_a")
        .orderBy(column("col_a"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInSelection() throws Exception {
    select()
        .from("table_a")
        .where(column("col_a").is().not().nul())
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a IS NOT NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInJoinConstraint() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on(column("table_a", "id").eq().column("table_b", "id_a"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (table_a.id == table_b.id_a)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInGroupByClause() throws Exception {
    select()
        .from("table_a")
        .groupBy(column("col_a"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInHavingClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having(sum(column("col_b")).gt().literal(0))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (SUM(col_b) > 0)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleColumnsFromSingleTable() throws Exception {
    select()
        .columns("col_a", "col_b", "col_c").of("table_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.col_a, table_a.col_b, table_a.col_c FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNullBuildByConvenienceMethod() throws Exception {
    select()
        .nul().as("col_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT NULL AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNumericLiteralBuildByConvenienceMethod() throws Exception {
    select()
        .literal(1500).as("col_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 1500 AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingObjectLiteralBuildByConvenienceMethod() throws Exception {
    select()
        .literal("test").as("col_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 'test' AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingFullyQualifiedTableBuildByConvenienceMethod() throws Exception {
    select()
        .column("table_a", "col_a")
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a"), eq(new String[0]));
  }

  private QueryBuilder buildComplexQuery() {
    return select()
        .column("table_a", "col_a")
        .from("table_a")
        .left().join("table_b").as("b")
        .on("b.id > ?", 1)
        .groupBy(column("b", "id"))
        .having(column("col_a").lt().arg(), 2)
        .limit(10)
        .offset(20)
        .orderBy(column("table_a", "col_a"))
        .except()
        .select()
        .distinct()
        .column("col_a")
        .from("table_a")
        .where(column("col_a").eq().literal(-1));
  }

  @Test
  public void shouldCopyQuery() throws Exception {
    Query originalQuery = buildComplexQuery().build();

    originalQuery.perform(mDb);
    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));

    QueryBuilder copy = originalQuery.buildUpon();

    copy.build().perform(mDb);
    verify(mDb, times(2)).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));
  }

  @Test
  public void shouldNotChangeOriginalQueryWhenChangingACopy() throws Exception {
    Query originalQuery = buildComplexQuery().build();

    originalQuery.perform(mDb);
    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));

    QueryBuilder copy = originalQuery.buildUpon();
    copy.where(column("a").is().not().nul());

    copy.build().perform(mDb);
    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) AND (a IS NOT NULL) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));

    originalQuery.perform(mDb);
    verify(mDb, times(2)).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));
  }

  @Test
  public void shouldNotChangeACopyWhenChangingTheOriginalQuery() throws Exception {
    QueryBuilder originalQueryBuilder = buildComplexQuery();
    Query originalQuery = originalQueryBuilder.build();

    originalQuery.perform(mDb);
    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));

    QueryBuilder copy = originalQuery.buildUpon();

    originalQueryBuilder.where(column("a").is().not().nul());
    originalQueryBuilder.build().perform(mDb);
    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) AND (a IS NOT NULL) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));

    copy.build().perform(mDb);
    verify(mDb, times(2)).rawQuery(eq("SELECT table_a.col_a FROM table_a LEFT JOIN table_b AS b ON (b.id > ?) GROUP BY b.id HAVING (col_a < ?) EXCEPT SELECT DISTINCT col_a FROM table_a WHERE (col_a == -1) ORDER BY table_a.col_a LIMIT 10 OFFSET 20"), eq(new String[] { "1", "2" }));
  }

  @Test
  public void shouldCopyTheQueryWithIncompleteJoinStatement() throws Exception {
    Query originalQuery = select()
        .from("table_a")
        .join("table_b")
        .build();

    QueryBuilder copy = originalQuery.buildUpon();

    originalQuery.perform(mDb);
    copy.build().perform(mDb);

    verify(mDb, times(2)).rawQuery(eq("SELECT * FROM table_a JOIN table_b"), eq(new String[0]));
  }

  @Test
  public void shouldCopyTheQueryWithMultipleJoinStatements() throws Exception {
    Query originalQuery = select()
        .from("table_a")
        .join("table_b")
        .join("table_c")
        .build();

    QueryBuilder copy = originalQuery.buildUpon();

    originalQuery.perform(mDb);
    copy.build().perform(mDb);

    verify(mDb, times(2)).rawQuery(eq("SELECT * FROM table_a JOIN table_b JOIN table_c"), eq(new String[0]));
  }

  @Test
  public void shouldOverrideSelectDistinctWithLaterCallToSelectAll() throws Exception {
    select()
        .distinct()
        .from("table_a")
        .all()
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldCopyTheQueryWithJoinStatementWithConstraint() throws Exception {
    Query originalQuery = select()
        .from("table_a")
        .join("table_b")
        .on(column("id").eq().column("id_a"))
        .build();

    QueryBuilder copy = originalQuery.buildUpon();

    originalQuery.perform(mDb);
    copy.build().perform(mDb);

    verify(mDb, times(2)).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (id == id_a)"), eq(new String[0]));
  }

  @Test
  public void shouldCopyTheQueryWithJoinStatementWithUsingClause() throws Exception {
    Query originalQuery = select()
        .from("table_a")
        .left().join("table_b")
        .using("id")
        .build();

    QueryBuilder copy = originalQuery.buildUpon();

    originalQuery.perform(mDb);
    copy.build().perform(mDb);

    verify(mDb, times(2)).rawQuery(eq("SELECT * FROM table_a LEFT JOIN table_b USING (id)"), eq(new String[0]));
  }

  @Test
  public void shouldGetListOfTablesForSimpleQuery() throws Exception {
    Set<String> tables = select().from("table_a").getTables();

    assertThat(tables).containsExactly("table_a");
  }

  @Test
  public void shouldGetListOfTablesFromSubqueries() throws Exception {
    Set<String> tables = select().from(select().from("table_a").build()).getTables();

    assertThat(tables).containsExactly("table_a");
  }

  @Test
  public void shouldGetListOfTablesFromJoins() throws Exception {
    Set<String> tables = select().from("table_a").join("table_b").getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetListOfTablesFromMultipleJoins() throws Exception {
    Set<String> tables = select().from("table_a").join("table_b").join("table_c").getTables();

    assertThat(tables).containsExactly("table_a", "table_b", "table_c");
  }

  @Test
  public void shouldGetListOfTablesFromJoinedSubqueries() throws Exception {
    Set<String> tables = select().from("table_a").join(select().from("table_b").build()).getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetListOfTablesForCompoundQuery() throws Exception {
    Set<String> tables =
        select().from("table_a")
            .union()
            .select().from("table_b")
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetTablesFromInExpressionInSelection() throws Exception {
    Set<String> tables =
        select()
            .from("table_a")
            .where(column("col_a").in(select().column("id_a").from("table_b").build()))
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetTablesFromInExpressionInHavingClause() throws Exception {
    Set<String> tables =
        select()
            .from("table_a")
            .groupBy("col_b")
            .having(column("col_a").in(select().column("id_a").from("table_b").build()))
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetTablesFromInExpressionInProjection() throws Exception {
    Set<String> tables =
        select()
            .expr(column("col_a").in(select().column("id_a").from("table_b").build()))
            .from("table_a")
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetTablesFromInExpressionInOrderBy() throws Exception {
    Set<String> tables =
        select()
            .from("table_a")
            .orderBy(column("col_a").in(select().column("id_a").from("table_b").build()))
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b");
  }

  @Test
  public void shouldGetTablesFromInExpressionInJoinConstraints() throws Exception {
    Set<String> tables =
        select()
            .from("table_a")
            .join("table_b")
            .on(column("table_b", "col_a").in(select().column("id_a").from("table_c").build()))
            .getTables();

    assertThat(tables).containsExactly("table_a", "table_b", "table_c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectExpressionInProjectionWithUnboundArgsPlaceholders() throws Exception {
    select().expr(column("col2").eq().arg());
  }

  @Test
  public void shouldBuildProjectionFromExpressionWithBoundArgs() throws Exception {
    select()
        .expr(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT col_a IN (SELECT id FROM table_b WHERE (status=?)) FROM table_a"), eq(new String[] { "new" }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooManyArgsPlaceholders() throws Exception {
    select().where(column("col2").eq().arg());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooFewArgsPlaceholders() throws Exception {
    select().where(column("col2").eq().arg(), 1, 2);
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithArgsPlaceholders() throws Exception {
    select()
        .from("table_a")
        .where(column("col_a").eq().arg(), "val2")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a == ?)"), eq(new String[] { "val2" }));
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithBoundArgs() throws Exception {
    select()
        .from("table_a")
        .where(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a IN (SELECT id FROM table_b WHERE (status=?)))"), eq(new String[] { "new" }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectJoinConstraintWithExpressionWithTooManyArgsPlaceholders() throws Exception {
    select().from("table_a").join("table_b").on(column("col2").eq().arg());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectJoinConstraintWithExpressionWithTooFewArgsPlaceholders() throws Exception {
    select().from("table_a").join("table_b").on(column("col2").eq().arg(), 1, 2);
  }

  @Test
  public void shouldBuildJoinConstraintFromExpressionWithArgsPlaceholders() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on(column("col_a").eq().arg(), "val2")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (col_a == ?)"), eq(new String[] { "val2" }));
  }

  @Test
  public void shouldBuildJoinConstraintFromExpressionWithBoundArgs() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (col_a IN (SELECT id FROM table_b WHERE (status=?)))"), eq(new String[] { "new" }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectHavingClauseWithExpressionWithTooManyArgsPlaceholders() throws Exception {
    select().from("table_a").groupBy("col_a").having(column("col2").eq().arg());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectHavingClauseWithExpressionWithTooFewArgsPlaceholders() throws Exception {
    select().from("table_a").groupBy("col_a").having(column("col2").eq().arg(), 1, 2);
  }

  @Test
  public void shouldBuildHavingClauseFromExpressionWithArgsPlaceholders() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having(column("col_b").eq().arg(), "val2")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b == ?)"), eq(new String[] { "val2" }));
  }

  @Test
  public void shouldBuildHavingClauseFromExpressionWithBoundArgs() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_a IN (SELECT id FROM table_b WHERE (status=?)))"), eq(new String[] { "new" }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectExpressionInGroupByWithUnboundArgsPlaceholders() throws Exception {
    select().groupBy(column("col2").eq().arg());
  }

  @Test
  public void shouldBuildGroupByFromExpressionWithBoundArgs() throws Exception {
    select()
        .from("table_a")
        .groupBy(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a IN (SELECT id FROM table_b WHERE (status=?))"), eq(new String[] { "new" }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectExpressionInOrderByWithUnboundArgsPlaceholders() throws Exception {
    select().orderBy(column("col2").eq().arg());
  }

  @Test
  public void shouldBuildOrderByFromExpressionWithBoundArgs() throws Exception {
    select()
        .from("table_a")
        .orderBy(column("col_a").in(select().column("id").from("table_b").where("status=?", "new").build()))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a IN (SELECT id FROM table_b WHERE (status=?))"), eq(new String[] { "new" }));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelection() throws Exception {
    select()
        .from("table_a")
        .where("col_a IS NULL", (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelectionWithExpression() throws Exception {
    select()
        .from("table_a")
        .where(column("col_a").is().nul(), (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForHaving() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b IS NULL", (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForHavingWithExpression() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having(column("col_b").is().nul(), (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForJoinConstraint() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on("col_b IS NULL", (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (col_b IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForJoinConstraintWithExpression() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on(column("col_b").is().nul(), (Object[]) null)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (col_b IS NULL)"), eq(new String[0]));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNullColumnListInJoinUsingClause() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .using((String[]) null)
        .build()
        .perform(mDb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyColumnListInJoinUsingClause() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .using(new String[0])
        .build()
        .perform(mDb);
  }

  @Test
  public void shouldPreserveOrderOfSpecifiedColumns() throws Exception {
    select()
        .column("a").columns("b", "c").column("d").from("table_a")
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a, b, c, d FROM table_a"), eq(new String[0]));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void shouldGracefullyHandleNullsInVariousPlaces() throws Exception {
    String[] projection = null;
    String selection = null;
    String selectionArgs = null;
    String sortOrder = null;

    select()
        .columns(projection)
        .from("table_a")
        .where(selection, selectionArgs)
        .orderBy(sortOrder)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldNotLoseArgumentsInJoinedSubqueries() throws Exception {
    select()
        .allColumns().from("table_a")
        .join(
            select().column("col_a").from("table_b").where(column("col_b").eq().arg(), "1500")
        )
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN (SELECT col_a FROM table_b WHERE (col_b == ?))"), eq(new String[] { "1500" }));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailIfNoTablesOrLiteralsWereSpecified() throws Exception {
    select().build().perform(mDb);
  }

  @Test
  public void shouldAllowQueryingSimpleLiteral() throws Exception {
    select().literal(1).build().perform(mDb);
    verify(mDb).rawQuery(eq("SELECT 1"), eq(new String[0]));
  }
}
