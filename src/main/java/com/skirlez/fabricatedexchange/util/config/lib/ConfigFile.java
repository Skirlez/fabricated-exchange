package com.skirlez.fabricatedexchange.util.config.lib;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;


/** A file that uses YAML to save and supports comments. */
public abstract class ConfigFile<T> extends AbstractFile<T> {
	private static final Yaml YAML;
	static {
		DumperOptions dumperOptions = new DumperOptions(); 
		dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
		dumperOptions.setProcessComments(true);
		YAML = new Yaml(dumperOptions);
	}

	private final Map<String, String[]> commentsMap;
	public ConfigFile(Type type, String name, Map<String, String[]> commentsMap) {
		super(type, name);
		this.commentsMap = commentsMap;
	}

	@Override
	protected T readValue(Reader reader) throws Exception {
		return YAML.loadAs(reader, (Class<?>)type);
	}

	@Override
	protected void writeValue(Writer writer, T value) throws Exception {
		Node root = YAML.represent(value);

		if (!commentsMap.isEmpty()) {
			// apply comments
			
			iterateOverKeys((MappingNode)root, new Consumer<ScalarNode>() {
				@Override
				public void accept(ScalarNode keyNode) {
					String key = keyNode.getValue();
					if (!commentsMap.containsKey(key))
						return;
					
					String[] commentStrings = commentsMap.get(key);
					List<CommentLine> comments = new ArrayList<CommentLine>(commentStrings.length);
					for (int i = 0; i < commentStrings.length; i++)
						comments.add(new CommentLine(null, null, commentStrings[i], CommentType.BLOCK));
					
					keyNode.setBlockComments(comments);
				}
			});
		}
		
		YAML.serialize(root, writer);
	}


	private void iterateOverKeys(MappingNode node, Consumer<ScalarNode> consumer) {
		for (NodeTuple nodeTuple : node.getValue()) {
			Node keyNode = nodeTuple.getKeyNode();
			if (keyNode.getTag().equals(Tag.MAP)) {
				iterateOverKeys(node, consumer);
				continue;
			}
			if (keyNode instanceof ScalarNode scalarNode)
				consumer.accept(scalarNode);
		}
	}
}

