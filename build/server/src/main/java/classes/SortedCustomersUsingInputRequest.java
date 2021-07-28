package classes;

import rest.GraphQLInputContext;

public class SortedCustomersUsingInputRequest implements rest.IGraphQLInput {
  public CustomerSortOptions sortBy;

  public SortedCustomersUsingInputRequest() {}

  public SortedCustomersUsingInputRequest(CustomerSortOptions sortBy) {
    this.sortBy = sortBy;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("sortBy")) {
      sortBy = ctx.readEnum("sortBy", CustomerSortOptions.class);
    }
  }
}
