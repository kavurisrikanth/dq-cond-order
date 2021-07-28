package lists;

import classes.CustomerUtil;
import classes.SortedCustomersUsingInput3;
import classes.SortedCustomersUsingInput3In;
import classes.SortedCustomersUsingInput3Request;
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
public class SortedCustomersUsingInput3Impl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public SortedCustomersUsingInput3Request inputToRequest(SortedCustomersUsingInput3In inputs) {
    SortedCustomersUsingInput3Request request = new SortedCustomersUsingInput3Request();
    request.sortBy = inputs.sortBy;
    return request;
  }

  public SortedCustomersUsingInput3 get(SortedCustomersUsingInput3In inputs) {
    SortedCustomersUsingInput3Request request = inputToRequest(inputs);
    return get(request);
  }

  public SortedCustomersUsingInput3 get(SortedCustomersUsingInput3Request request) {
    List<NativeObj> rows = getNativeResult(request);
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(0), Customer.class));
    }
    SortedCustomersUsingInput3 wrap = new SortedCustomersUsingInput3();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInput3In inputs) throws Exception {
    SortedCustomersUsingInput3Request request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInput3Request request)
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

  public void processOrderBy(List<NativeObj> result, SortedCustomersUsingInput3Request inputs) {
//    Function<NativeObj, Comparable<Object>> order =
//        (c) -> 1;
//    result.sort(Comparator.comparing(order));
  }

  public List<NativeObj> getNativeResult(SortedCustomersUsingInput3Request request) {
    String sql = "select a._id a0 from _customer a";
    Query query = em.createNativeQuery(sql);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 0);
    processOrderBy(result, request);
    return result;
  }
}
