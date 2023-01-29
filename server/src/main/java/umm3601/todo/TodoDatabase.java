package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
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

    // Filter the todos by category
    if (queryParams.containsKey("category")) {
      String category = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCategory(filteredTodos, category);
    }

    // Filter the todos by owner
    if (queryParams.containsKey("owner")) {
      String owner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, owner);
    }

    // Filter todos by if they contain a given substring
    if (queryParams.containsKey("contains")) {
      for (String contained : queryParams.get("contains")) {
        filteredTodos = filterTodosByContainment(filteredTodos, contained);
      }
    }

    // Filter status if it is set
    if (queryParams.containsKey("status")) {
      String status = queryParams.get("status").get(0);
      boolean statusBool = false;
      if (status.equals("complete")) {
        statusBool = true;
      } else if (status.equals("incomplete")) {
        statusBool = false;
      } else {
        throw new BadRequestResponse("Specified status '" + status + "' is not 'complete' or 'incomplete'");
      }

      filteredTodos = filterTodosByCompleteness(filteredTodos, statusBool);
    }

    // Truncate response if limit is set
    if (queryParams.containsKey("limit")) {
      String limitText = queryParams.get("limit").get(0);
      try {
        int limit = Integer.parseInt(limitText);
        filteredTodos = Arrays.copyOfRange(filteredTodos, 0, limit);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + limitText + "' can't be parsed to an integer");
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

  /**
   * Get an array of all the todos containing the given string.
   */
  public Todo[] filterTodosByContainment(Todo[] todos, String value) {
    return Arrays.stream(todos).filter(x -> (x.body.contains(value))).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target owner.
   */
  public Todo[] filterTodosByOwner(Todo[] todos, String owner) {
    return Arrays.stream(todos).filter(x -> (x.owner.equals(owner))).toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target category.
   */
  public Todo[] filterTodosByCategory(Todo[] todos, String category) {
    return Arrays.stream(todos).filter(x -> (x.category.equals(category))).toArray(Todo[]::new);
  }
}
