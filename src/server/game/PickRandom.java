package server.game;


import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import common.dto.KeywordDto;
import server.dao.KeywordDao;

public class PickRandom { 
	private Random random = new Random();
	
	private KeywordDao keywordDao;
	public PickRandom(KeywordDao keywordDao) {
		this.keywordDao = keywordDao;
	}
	
	//category와 keyword 선택 후 반환
	public KeywordDto pick(String difficulty) throws SQLException{
		int categoryId = pickCategoryId();
		return pickKeyword(categoryId, difficulty);
	}
	
	//category 선택
	private int pickCategoryId() throws SQLException{
		List<Integer> categoryIds = keywordDao.getCategoryIds();
		int index = random.nextInt(categoryIds.size());
		return categoryIds.get(index);
	}
	
	//keyword 선택
	private KeywordDto pickKeyword(int categoryId, String difficulty) throws SQLException{
		List<KeywordDto> keywords = keywordDao.getKeywords(categoryId, difficulty);
		int index = random.nextInt(keywords.size());
		return keywords.get(index);
	}
	
	public String pickOtherKeyword(KeywordDto selected) throws SQLException{
		List<KeywordDto> keywords = keywordDao.getKeywords(selected.getCategoryId(), selected.getDifficulty());
		
		for(int i = 0; i< keywords.size(); i++) {
			if(keywords.get(i).getKeywordId() == selected.getKeywordId()) {
				keywords.remove(i);
				break;
			}
		}
		int index = random.nextInt(keywords.size());
		
		return keywords.get(index).getKeyword();
	}
}