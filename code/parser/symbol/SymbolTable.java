package parser.symbol;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class SymbolTableEntry {
    private String name;
    private String type;
    private String scope;

    public SymbolTableEntry(String name, String type, String scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }
}
public class SymbolTable {
    private Map<String, SymbolTableEntry> entries;
    private Stack<String> scopeStack;

    //initialization of the class
    public SymbolTable() {
        this.entries = new HashMap<>();
        this.scopeStack = new Stack<>();
    }

    //method for entering a scope
    public void enterScope(String scopeName) {
        scopeStack.push(scopeName);
    }

    //method for exiting a scope
    public void exitScope() {
        String currentScope = scopeStack.pop();
        // remove entries specific to the scope (OPTIONAL, SUBJECT TO REMOVAL IF FOUND UNNECESSARY)
        entries.entrySet().removeIf(entry -> entry.getValue().getScope().equals(currentScope));
    }

    //method for adding a new symbol to the table
    public boolean insert(String name, String type) {
        String currentScope = scopeStack.isEmpty() ? "global" : scopeStack.peek();
        String scopedName = currentScope + "::" + name;
    
        if (entries.containsKey(scopedName)) {
            return false; // Symbol already exists in this scope.
        }
    
        SymbolTableEntry entry = new SymbolTableEntry(name, type, currentScope);
        entries.put(scopedName, entry);
        return true;
    }

    //method for getting a symbol from the table
    public SymbolTableEntry lookup(String symbolName) {
        Stack<String> tempStack = new Stack<>();
        tempStack.addAll(scopeStack);
    
        while (!tempStack.isEmpty()) {
            String currentScope = tempStack.peek();
            String scopedName = currentScope + "::" + symbolName;
    
            if (entries.containsKey(scopedName)) {
                return entries.get(scopedName);
            }
    
            // Move to the next enclosing scope
            tempStack.pop();
        }
    
        // If not found in any scope, check global scope
        String globalScopedName = "global::" + symbolName;
        return entries.get(globalScopedName); // Will return null if not found
    }

    //method for checking the type of a symbol
    public boolean checkType(String symbolName, String expectedType) {
        SymbolTableEntry entry = lookup(symbolName); // Find the symbol in the table.
        if (entry == null) {
            throw new RuntimeException("Symbol '" + symbolName + "' not found."); //to be replaced with Syntax Error Handler
        }
        return entry.getType().equals(expectedType);
    }

    //method for clearing the table
    public void clearTable() {
        entries.clear();
        scopeStack.clear();
    }

}
