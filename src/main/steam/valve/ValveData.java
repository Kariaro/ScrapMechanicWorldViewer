package valve;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hardcoded.util.FileUtils;

/**
 * 
 * Took some inspiration from the valve website.
 * https://developer.valvesoftware.com/wiki/KeyValues
 * 
 * @author HardCoded
 */
public class ValveData {
	private Map<String, ValveData> groups = new HashMap<>();
	private Map<String, String> values = new HashMap<>();
	private String[] lines;
	private int level;
	private int start;
	private int end;
	
	public ValveData(File file) {
		String content = FileUtils.readFile(file);
		lines = content.split("\\r?\\n");
		for(int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
		}
		
		start = 0;
		level = 0;
		end = lines.length;
		
		load();
	}
	
	private ValveData(String[] lines, int level, int start, int end) {
		this.lines = lines;
		this.level = level;
		this.start = start;
		this.end = end;
		load();
	}
	
	private void load() {
		int begin = 0;
		int top = level;
		
		String name = null;
		
		for(int i = start; i < end; i++) {
			String line = lines[i];
			
			if(top == level) {
				boolean isGroup = isGroup(i);
				
				// Empty line or value
				List<String> field = processLine(line);
				
				if(isGroup) {
					name = field.get(0);
					begin = i + 2;
					continue;
				} else {
					
					// A field must have 2 elements, "key": "value"
					if(field.size() == 2) {
						values.put(field.get(0), field.get(1));
					}
				}
			}
			
			if(line.equals("{")) top++;
			if(line.equals("}")) {
				top--;
				
				if(top == level) {
					groups.put(name, new ValveData(lines, top + 1, begin, i));
				}
			}
		}
	}
	
	private List<String> processLine(String line) {
		List<String> values = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		boolean quoted = false;
		
		for(int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			if(escaped) {
				switch(c) {
					case 'n':
						sb.append('\n');
						break;
					case 't':
						sb.append('\t');
						break;
					case '\\':
						sb.append('\\');
						break;
					case '"':
						sb.append('"');
						break;
					default:
						// throw new InvalidEscapeException ???
				}
				
				escaped = false;
				continue;
			}
			
			if(c == '\\') {
				escaped = true;
				continue;
			}
			
			if(c == '"') {
				if(quoted) {
					values.add(sb.toString());
					sb.delete(0, sb.length());
				}
				
				quoted = !quoted;
			} else if(quoted) {
				sb.append(c);
			} else {
				if(c != ' ' || c != '\t') {
					// throw new InvalidSpaceCharacter ???
				}
			}
		}
		
		return values;
	}
	
	private boolean isGroup(int lineIndex) {
		if(lineIndex + 1 >= lines.length) return false;
		String next = lines[lineIndex + 1].trim();
		if(next.equals("{")) return true;
		return false;
	}
	
	public ValveData get(String... keys) {
		return get(keys, 0);
	}
	
	public String getValue(String... keys) {
		return getValue(keys, 0);
	}
	
	public Set<String> getValueNames() {
		return values.keySet();
	}
	
	public Set<String> getGroupNames() {
		return groups.keySet();
	}
	
	private ValveData get(String[] keys, int offset) {
		if(offset < keys.length) {
			String key = keys[offset];
			ValveData data = groups.getOrDefault(key, null);
			if(data == null) return null;
			
			return data.get(keys, offset + 1);
		}
		
		return this;
	}
	
	private String getValue(String[] keys, int offset) {
		String key = keys[offset];
		if(offset + 1 < keys.length) {
			ValveData data = groups.getOrDefault(key, null);
			if(data == null) return null;
			
			return data.getValue(keys, offset + 1);
		}
		
		return values.getOrDefault(key, null);
	}
}
