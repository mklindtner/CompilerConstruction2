import java.util.HashMap;
import java.util.Map.Entry;

class Environment {
	private HashMap<String, Value> variableValues = new HashMap<>();

	public Environment() {
	}

	public void setVariable(String name, Value value) {
		variableValues.put(name, value);
	}

	public Value getVariable(String name) {
		Value value = variableValues.get(name);
		if (value == null) {
			System.err.println("Variable not defined: " + name);
			System.exit(-1);
		}
		return value;
	}

	public String toString() {
		String table = "";
		for (Entry<String, Value> entry : variableValues.entrySet()) {
			table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
		}
		return table;
	}
}
