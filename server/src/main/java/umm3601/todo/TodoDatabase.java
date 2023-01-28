package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.BadRequestResponse;

/*
 * A fake "database" of the todo's loaded from file
 */
public class TodoDatabase {
  private Todo[] allTodos;

  public TodoDatabase(String todoDataFilename) throws IOException {
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(todoDataFilename));
    ObjectMapper objectMapper = new ObjectMapper();
    allTodos = objectMapper.readValue(reader, Todo[].class);
  }

  public int size() {
    return allTodos.length;
  }

  public Todo[] listTodos(Map<String, List<String>> queryParams) {
    Todo[] filteredTodos = allTodos;

    // Filter status if it is set
    if (queryParams.containsKey("status")) {
      boolean status = queryParams.get("status").get(0).equals("complete");
      filteredTodos = filterTodosByCompleteness(filteredTodos, status);
    }

    // Truncate response if limit is set
    if (queryParams.containsKey("limit")) {
      try {
        int limit = Integer.parseInt(queryParams.get("limit").get(0));
        filteredTodos = Arrays.copyOfRange(filteredTodos, 0, limit);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + queryParams.get("limit").get(0) + "' can't be parsed to an integer");
      }
    }

    return filteredTodos;
  }

  public Todo getTodo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * Get an array of all the todos having the target completeness status.
   */
  public Todo[] filterTodosByCompleteness(Todo[] todos, boolean completeness) {
    return Arrays.stream(todos).filter(x -> (x.status == completeness)).toArray(Todo[]::new);
  }
}
