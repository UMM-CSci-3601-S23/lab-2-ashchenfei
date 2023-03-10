package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;

import umm3601.Server;

@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {
  private Context ctx = mock(Context.class);

  private TodoController todoController;
  private static TodoDatabase db;

  @BeforeEach
  public void setUp() throws IOException {
    db = new TodoDatabase(Server.TODO_DATA_FILE);
    todoController = new TodoController(db);
  }

  /**
   * Confirms that we can get all the users.
   *
   * @throws IOException
   */
  @Test
  public void canGetAllTodos() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the users
    // back.
    todoController.getTodos(ctx);

    // Confirm that `json` was called with all the users.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(db.size(), argument.getValue().length);
  }

  @Test
  public void canGetTodoWithSpecifiedId() throws IOException {
    String id = "58895985c2fc014023fbc272";
    Todo todo = db.getTodo(id);

    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
    assertEquals("Velit ut amet esse esse. Pariatur quis aute minim incididunt.", todo.body);
  }

  @Test
  public void canGetTodoWithSpecifiedIdNoExist() throws IOException {
    String id = "ThisIDWon'tExist";
    Todo todo = db.getTodo(id);

    assertEquals(todo, null);

    when(ctx.pathParam("id")).thenReturn(id);

    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });
    assertEquals("No todo with id " + id + " was found.", exception.getMessage());
  }

  @Test
  public void canLimitResponseLength() throws IOException {
    Integer testLimit = 7;

    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {testLimit.toString()}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that the number of responded todos is less than or equal to the requested number
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertTrue(argument.getValue().length <= testLimit);
  }

  @Test
  public void canGetTodosWithStatusComplete() throws IOException {
    // Add a query param map to the context that maps "status"
    // to "complete".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // status complete.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have status complete.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.status);
    }

  }

  @Test
  public void canGetTodosWithStatusIncomplete() throws IOException {
    // Add a query param map to the context that maps "status"
    // to "incomplete".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // status incomplete.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have status complete.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertFalse(todo.status);
    }

  }

  @Test
  public void canFilterTodosByOwner() throws IOException {
    // Add a query param map to the context that maps "owner"
    // to "Roberta".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Roberta"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // owner Roberta.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have the correct owner.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.owner.equals("Roberta"));
    }

  }

  @Test
  public void canFilterTodosByCategory() throws IOException {
    // Add a query param map to the context that maps "category"
    // to "software design".
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"software design"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map to limit the result to just todos with
    // category software design.
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have the correct category.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.category.equals("software design"));
    }

  }

  @Test
  public void respondsAppropriatelyToIllegalStatus() {
    // We'll set the requested "status" to be a different string ("hello")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"hello"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has an age that can't be parsed to a number.
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified status '" + "hello" + "' is not 'complete' or 'incomplete'", exception.getMessage());
  }

  @Test
  public void respondsAppropriatelyToIllegalLimit() {
    // We'll set the requested "limit" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {"abc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has an age that can't be parsed to a number.
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
    assertEquals("Specified limit '" + "abc" + "' can't be parsed to an integer", exception.getMessage());
  }

  @Test
  public void canGetTodosByContainmentSingle() {
    // We'll set the requested "status" to be a different string ("hello")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {" eu "}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` contain the given word
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.body.contains(" eu "));
    }
  }

  @Test
  public void canGetTodosByContainmentMultiple() {
    // We'll set the requested "status" to be a different string ("hello")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {" eu ", " et "}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the method on the mock controller with the added
    // query param map
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` contain the given word
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (Todo todo : argument.getValue()) {
      assertTrue(todo.body.contains(" eu "));
      assertTrue(todo.body.contains(" et "));
    }
  }

  @Test
  public void respondsAppropriatelyToIllegalOrderAttribute() {
    // We'll set the requested "orderBy" to be a string ("abc")
    // that is not an attribute.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"abc"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has a string that can't be parsed to an attribute.
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });

    assertEquals("Cannot sort by attribute 'abc'", exception.getMessage());

  }

  @Test
  public void canSortByCategory() {
    // We'll set the requested "orderBy" to be a string ("category")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"category"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    Todo[] sortedTodos = argument.getValue();
    Todo[] clonedTodos = Arrays.stream(argument.getValue()).sorted(
      Comparator.comparing(Todo::getCategory)).toArray(Todo[]::new);

    for (int i = 0; i < clonedTodos.length; i++) {
      assertTrue(sortedTodos[i].equals(clonedTodos[i]));
    }
  }

  @Test
  public void canSortByBody() {
    // We'll set the requested "orderBy" to be a string ("body")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"body"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    Todo[] sortedTodos = argument.getValue();
    Todo[] clonedTodos = Arrays.stream(argument.getValue()).sorted(
      Comparator.comparing(Todo::getBody)).toArray(Todo[]::new);

    for (int i = 0; i < clonedTodos.length; i++) {
      assertTrue(sortedTodos[i].equals(clonedTodos[i]));
    }
  }

  @Test
  public void canSortByStatus() {
    // We'll set the requested "orderBy" to be a string ("status")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"status"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    Todo[] sortedTodos = argument.getValue();
    Todo[] clonedTodos = Arrays.stream(argument.getValue()).sorted(
      Comparator.comparing(Todo::getStatus)).toArray(Todo[]::new);

    for (int i = 0; i < clonedTodos.length; i++) {
      assertTrue(sortedTodos[i].equals(clonedTodos[i]));
    }
  }

  @Test
  public void canSortByOwner() {
    // We'll set the requested "orderBy" to be a string ("owner")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"owner"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    Todo[] sortedTodos = argument.getValue();
    Todo[] clonedTodos = Arrays.stream(argument.getValue()).sorted(
      Comparator.comparing(Todo::getOwner)).toArray(Todo[]::new);

    for (int i = 0; i < clonedTodos.length; i++) {
      assertTrue(sortedTodos[i].equals(clonedTodos[i]));
    }
  }

  @Test
  public void canHandleCombination() {
    // We'll set the requested "orderBy" to be a string ("owner")
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"category"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    queryParams.put("owner", Arrays.asList(new String[] {"Blanche"}));
    queryParams.put("limit", Arrays.asList(new String[] {"12"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    Todo[] sortedTodos = argument.getValue();
    Todo[] clonedTodos = Arrays.stream(argument.getValue()).sorted(
      Comparator.comparing(Todo::getCategory)).toArray(Todo[]::new);

    for (int i = 0; i < clonedTodos.length; i++) {
      // Ensure the todo is complete
      assertTrue(sortedTodos[i].status);

      // Ensure Blanche is the owner
      assertTrue(sortedTodos[i].owner.equals("Blanche"));

      // Ensure the todos are sorted by category
      assertTrue(sortedTodos[i].equals(clonedTodos[i]));
    }

    // Ensure the limiting limited the number of responses to 12
    assertTrue(clonedTodos.length <= 12);
  }
}


