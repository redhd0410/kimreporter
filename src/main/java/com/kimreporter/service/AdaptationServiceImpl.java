package com.kimreporter.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kimreporter.controller.AdaptationController;
import com.kimreporter.controller.UserInfoController;
import com.kimreporter.domain.AdaptationVO;
import com.kimreporter.persistence.AdaptationDAO;

@Service
public class AdaptationServiceImpl implements AdaptationService{
	
	@Inject 
	private AdaptationDAO dao;

	@Override
	public void regist(AdaptationVO adaptation) throws Exception {	
		// 초기 변수 
		String url = "https://media.daum.net/ranking/popular/";
		String news_url = "https://news.v.daum.net/v/";
		ArrayList<ArrayList<String> > summarized_news = new ArrayList<ArrayList<String> >();
		ArrayList<String> links_array = new ArrayList<String> ();
		List<AdaptationVO > all_list = dao.listAll();
		
		try {
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.getElementsByClass("tit_thumb").select("a[href]");
			Elements rankings = doc.select("ul.list_news2").select("span.screen_out");
			// 링크 id (맨 뒤에 17숫자)를 찾아서 저장  
			int article_counter = 0;
			int i = 0;
			
			// 요약본이 없으면 스킵하고 있으면 리스트에 20개가 될 때까지 추가함
			while(article_counter < 20) {
				ArrayList<String> news_links = new ArrayList<String>();
				String link = links.get(i).attr("abs:href").toString();
				String rank = rankings.get(i).text();
				String parsed_link = link.substring(link.lastIndexOf("/") + 1);
				links_array.add(parsed_link);
				Document opened_link = Jsoup.connect(news_url+ parsed_link).get();
				Elements summary = opened_link.getElementsByClass("layer_util layer_summary").select("p");
				if (summary.text().isEmpty()) {
					
				}
				else {
					article_counter++;
					Elements news_title = opened_link.getElementsByClass("tit_view").select("h3");
					news_links.add(parsed_link);
					news_links.add(rank);
					news_links.add(news_title.text());
					news_links.add(summary.text());
					summarized_news.add(news_links);
				}
				i++;
			}
			
			// 업데이트문 1: 현재 랭킹에 있는 뉴스 순위 업데이트 
			for (ArrayList<String> arr:summarized_news) {
				
				String idx = arr.get(0);
				AdaptationVO vo = dao.read(idx);
				
				if (vo != null) {
					vo.setRanking(Integer.valueOf(arr.get(1)));
					dao.updateRanking(vo);
				} else {
					dao.create(adaptation, arr.get(2), arr.get(3), arr.get(0), Integer.valueOf(arr.get(1)));
				}
			}
			
			// 업데이트문 2: 현재 랭킹에 없는 뉴스 -1로 설정 
			for (AdaptationVO vo:all_list) {
				if (links_array.contains(vo.getAdaptation_id()) == false) {
					vo.setRanking(-1);
					dao.updateRanking(vo);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public AdaptationVO read(String adaptation_id) throws Exception {
		return dao.read(adaptation_id);
	}

	@Override
	public void modify(AdaptationVO adaptation) throws Exception {
		dao.update(adaptation);
		
	}

	@Override
	public void delete() throws Exception {
		// 일주일 지난 기사 다 삭제
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		String week = format1.format(cal.getTime());

		dao.delete(week);
		
	}

	@Override
	public List<AdaptationVO> listAll() throws Exception {
		return dao.listAll();
	}

}
