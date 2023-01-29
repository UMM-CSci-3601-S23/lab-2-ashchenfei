package umm3601.todo;

/// An individual todo object.
@SuppressWarnings({"VisibilityModifier"})
public class Todo {
  @SuppressWarnings({"MemberName"})
  public String _id;
  public String owner;
  public boolean status; // False when not yet completed, true if completed
  public String body;
  public String category;

  public String getOwner() {
    return owner;
  }

  public String getCategory() {
    return category;
  }

  public String getBody() {
    return body;
  }

  public boolean getStatus() {
    return status;
  }
}
