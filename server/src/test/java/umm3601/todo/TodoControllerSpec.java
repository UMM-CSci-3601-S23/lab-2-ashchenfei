package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
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
}


