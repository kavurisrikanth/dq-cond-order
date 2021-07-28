package lists;

import classes.CustomerSortOptions;
import classes.SortedCustomersUsingInput;
import classes.SortedCustomersUsingInputIn;
import classes.SortedCustomersUsingInputRequest;
import gqltosql.GqlToSql;
import gqltosql.SqlRow;
import graphql.language.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.Customer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.AbstractQueryService;

@Service
public class SortedCustomersUsingInputImpl extends AbsDataQueryImpl {
  @Autowired private EntityManager em;
  @Autowired private GqlToSql gqlToSql;

  public SortedCustomersUsingInputRequest inputToRequest(SortedCustomersUsingInputIn inputs) {
    SortedCustomersUsingInputRequest request = new SortedCustomersUsingInputRequest();
    request.sortBy = inputs.sortBy;
    return request;
  }

  public SortedCustomersUsingInput get(SortedCustomersUsingInputIn inputs) {
    SortedCustomersUsingInputRequest request = inputToRequest(inputs);
    return get(request);
  }

  public SortedCustomersUsingInput get(SortedCustomersUsingInputRequest request) {
    List<NativeObj> rows = getNativeResult(request);
    List<Customer> result = new ArrayList<>();
    for (NativeObj _r1 : rows) {
      result.add(NativeSqlUtil.get(em, _r1.getRef(0), Customer.class));
    }
    SortedCustomersUsingInput wrap = new SortedCustomersUsingInput();
    wrap.items = result;
    return wrap;
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInputIn inputs) throws Exception {
    SortedCustomersUsingInputRequest request = inputToRequest(inputs);
    return getAsJson(field, request);
  }

  public JSONObject getAsJson(Field field, SortedCustomersUsingInputRequest request)
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

  public List<NativeObj> getNativeResult(SortedCustomersUsingInputRequest request) {
    String sql =
        "select a._id a0 from _customer a left join _customer b on b._id = a._guardian_id order by case when :param_0 = :param_1 then case when a._is_under_age then a._age_in_years else b._age_in_years end else a._age_in_years end";
    Query query = em.createNativeQuery(sql);
    setParameter(query, "param_0", request.sortBy);
    setParameter(query, "param_1", CustomerSortOptions.AGE);
    this.logQuery(sql, query);
    List<NativeObj> result = NativeSqlUtil.createNativeObj(query.getResultList(), 0);
    return result;
  }
}
