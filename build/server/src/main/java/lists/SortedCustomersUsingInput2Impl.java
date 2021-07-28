package lists;

import classes.CustomerSortOptions;
import classes.SortedCustomersUsingInput2;
import classes.SortedCustomersUsingInput2In;
import classes.SortedCustomersUsingInput2Request;
import d3e.core.IntegerExt;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.Customer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;

@Service
public class SortedCustomersUsingInput2Impl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public SortedCustomersUsingInput2Request inputToRequest(SortedCustomersUsingInput2In inputs) {
    SortedCustomersUsingInput2Request request = new SortedCustomersUsingInput2Request();
    request.sortBy = inputs.sortBy;
    return request;
  }

  public SortedCustomersUsingInput2 get(SortedCustomersUsingInput2In inputs) {
    SortedCustomersUsingInput2Request request = inputToRequest(inputs);
    return get(request);
  }

  public SortedCustomersUsingInput2 get(SortedCustomersUsingInput2Request request) {
    List<NativeObj> rows = getNativeResult(request);
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(4), Customer.class));
    }
    SortedCustomersUsingInput2 wrap = new SortedCustomersUsingInput2();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInput2In inputs) throws Exception {
    SortedCustomersUsingInput2Request request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInput2Request request)
      throws Exception {
    List<NativeObj> rows = getNativeResult(request);
    return getAsJson(field, rows);
  }

  public JSONObject getAsJson(Field field, List<NativeObj> rows) throws Exception {
    JSONArray array = new JSONArray();
    List<SqlRow> sqlDecl0 = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      array.put(NativeSqlUtil.getJSONObject(_r1, sqlDecl0));
    }
    gqlToSql.execute("Customer", AbstractQueryService.inspect(field, ""), sqlDecl0);
    JSONObject result = new JSONObject();
    result.put("items", array);
    return result;
  }

  public void processOrderBy(List<NativeObj> result, SortedCustomersUsingInput2Request inputs) {
    Function<NativeObj, String> order =
        (c) ->
            inputs.sortBy == CustomerSortOptions.AGE
                ? (c.getBoolean(0)
                    ? IntegerExt.toString(c.getInteger(1))
                    : IntegerExt.toString(c.getInteger(2)))
                : (inputs.sortBy == CustomerSortOptions.NAME
                    ? c.getString(3)
                    : IntegerExt.toString(c.getInteger(2)));
    result.sort(Comparator.comparing(order));
  }

  public List<NativeObj> getNativeResult(SortedCustomersUsingInput2Request request) {
    String sql =
        "select a._is_under_age a0, b._age_in_years a1, a._age_in_years a2, a._name a3, a._id a4 from _customer a left join _customer b on b._id = a._guardian_id";
    Query query = em.createNativeQuery(sql);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 4);
    processOrderBy(result, request);
    return result;
  }
}
