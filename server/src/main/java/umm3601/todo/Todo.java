package umm3601.todo;

/// An individual todo object.
public class Todo {
  @SuppressWarnings({"MemberName"})
  public String _id;
  public String owner;
  public boolean status; // False when not yet completed, true if completed
  public String body;
  public String category;
}
