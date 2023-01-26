package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
//import io.javalin.http.BadRequestResponse;

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

    return allTodos;
  }
}
