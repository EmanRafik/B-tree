package eg.edu.alexu.csd.filestructure.btree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MySearchEngine implements ISearchEngine {
	private HashMap<String, HashMap<String, Integer>> docs = new HashMap<>();
	private IBTree<String, HashMap<String, Integer>> tree;

	public MySearchEngine(int t) {
		tree = new BTree<String, HashMap<String, Integer>>(t);
	}

	@Override
	public void indexWebPage(String filePath) {
		if (filePath == null || filePath.isEmpty())
			throw new RuntimeErrorException(new Error());
		File file = new File(filePath);
		if (!file.exists())
			throw new RuntimeErrorException(new Error());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = builder.parse(filePath);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nList = doc.getElementsByTagName("doc");
		for (int i = 0; i < nList.getLength(); i++) {
			Element element = (Element) nList.item(i);
			String id = element.getAttribute("id");
			String text = element.getTextContent();
			text = text.trim();
			String[] words = text.split("\\s+");
			for (String word : words) {
				if (docs.containsKey(word.toLowerCase())) {
					if (docs.get(word.toLowerCase()).containsKey(id)) {
						docs.get(word.toLowerCase()).put(id, docs.get(word.toLowerCase()).get(id) + 1);
					} else {
						docs.get(word.toLowerCase()).put(id, 1);
					}
				} else {
					HashMap<String, Integer> map = new HashMap<>();
					map.put(id, 1);
					docs.put(word.toLowerCase(), map);
				}
			}
		}
		for (String word : docs.keySet()) {
			HashMap<String, Integer> m = tree.search(word);
			if (m == null) {
				tree.insert(word, docs.get(word));
			} else {
				for (String id : docs.get(word).keySet()) {
					m.put(id, docs.get(word).get(id));
				}
			}
		}
	}

	@Override
	public void indexDirectory(String directoryPath) {
		if (directoryPath == null || directoryPath.isEmpty())
			throw new RuntimeErrorException(new Error());
		File folder = new File(directoryPath);
		if (!folder.exists())
			throw new RuntimeErrorException(new Error());
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				indexWebPage(directoryPath + "//" + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				indexDirectory(directoryPath + "/" + listOfFiles[i].getName());
			}
		}
	}

	@Override
	public void deleteWebPage(String filePath) {
		if (filePath == null || filePath.isEmpty())
			throw new RuntimeErrorException(new Error());
		File file = new File(filePath);
		if (!file.exists())
			throw new RuntimeErrorException(new Error());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = builder.parse(filePath);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, HashMap<String, Integer>> deletedDocs = new HashMap<>();
		NodeList nList = doc.getElementsByTagName("doc");
		for (int i = 0; i < nList.getLength(); i++) {
			Element element = (Element) nList.item(i);
			String id = element.getAttribute("id");
			String text = element.getTextContent();
			text = text.trim();
			String[] words = text.split("\\s+");
			for (String word : words) {
				if (deletedDocs.containsKey(word.toLowerCase())) {
					if (deletedDocs.get(word.toLowerCase()).containsKey(id)) {
						deletedDocs.get(word.toLowerCase()).put(id, deletedDocs.get(word.toLowerCase()).get(id) + 1);
					} else {
						deletedDocs.get(word.toLowerCase()).put(id, 1);
					}
				} else {
					HashMap<String, Integer> map = new HashMap<>();
					map.put(id, 1);
					deletedDocs.put(word.toLowerCase(), map);
				}
			}
		}
		for (String word : deletedDocs.keySet()) {
			//HashMap<String, Integer> clone = (HashMap<String, Integer>) docs.get(word).clone();
			HashMap<String, Integer> m = tree.search(word);
			if (m != null) {
				for (String id : deletedDocs.get(word).keySet()) {
					if(m.containsKey(id)) {
					m.remove(id);
					}
				}
			}
		}

	}

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		if (word == null)
			throw new RuntimeErrorException(new Error());
		if (word.isEmpty())
			return new ArrayList<ISearchResult>();
		HashMap<String, Integer> m = tree.search(word.toLowerCase());
		if (m == null) {
			return null;
		} else {
			List<ISearchResult> searchResults = new ArrayList<>();
			for (String id : m.keySet()) {
				ISearchResult sr = new SearchResult(id,m.get(id));
				sr.setId(id);
				sr.setRank(m.get(id));
				searchResults.add(sr);
			}
			return searchResults;
		}
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		// TODO Auto-generated method stub
		if (sentence == null) {
			throw new RuntimeErrorException(new Error());
		}
		sentence = sentence.trim();
		sentence = sentence.toLowerCase();
		String[] words = sentence.split("\\s+");
		List<List<ISearchResult>> results = new ArrayList<>();
		List<ISearchResult> searchResult = new ArrayList<>();
		for (String word : words) {
			results.add(searchByWordWithRanking(word));
		}
		for (int i = 0; i < results.get(0).size(); i++) {
			int min = results.get(0).get(i).getRank();
			boolean flag = false;
			for (int j = 1; j < results.size(); j++) {
				flag = false;
				for (int k = 0; k < results.get(j).size(); k++) {
					if (results.get(j).get(k).getId().equals(results.get(0).get(i).getId())) {
						min = Math.min(min, results.get(j).get(k).getRank());
						flag = true;
						break;
					}
				}
				if (!flag) {
					break;
				}
			}
			if (flag) {
				ISearchResult s = new SearchResult(results.get(0).get(i).getId(),min);
				s.setId(results.get(0).get(i).getId());
				s.setRank(min);
				searchResult.add(s);
			}
		}
		return searchResult;
	}
}
