package umm3601.todo;

import io.javalin.http.Context;
//import io.javalin.http.HttpStatus;
//import io.javalin.http.NotFoundResponse;

/**
 * Controller that manages requests for todos.
 */
public class TodoController {
  private TodoDatabase database;

  /*
   * Construct a controller for the todos.
   */
  public TodoController(TodoDatabase database) {
    this.database = database;
  }

  /*
   * Produce a JSON response with a list of all the todo entries in the "database".
   */
  public void getTodos(Context ctx) {
    Todo[] todos = database.listTodos(ctx.queryParamMap());
    ctx.json(todos);
  }
}
