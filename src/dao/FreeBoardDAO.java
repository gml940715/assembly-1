package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dto.FreeBoardDTO;

public class FreeBoardDAO {
	private Connection getConnection()throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String user = "kh";
		String pw = "kh";

		return DriverManager.getConnection(url, user, pw);
	}

	public int insert(FreeBoardDTO param)throws Exception{ // 내용 등록
		String sql = "insert into FreeBoard values(FreeBoard_seq.nextval,?,?,?,0,?,default,?,?)";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql); 
				){
			pstat.setString(1, param.getTitle());
			pstat.setString(2, param.getContent());
			pstat.setString(3, param.getWriter());
			pstat.setString(4, param.getIp());
			pstat.setString(5, param.getEmail());
			pstat.setInt(6, param.getId());
			con.commit();
			int result = pstat.executeUpdate();
			return result;

		}
	}

	public List<FreeBoardDTO> FreeList()throws Exception{ // 자유게시판 글 목록
		String sql = "select * from FreeBoard order by seq desc";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();
				){
			List<FreeBoardDTO> freeList = new ArrayList<>();
			while(rs.next()) {
				int seq = rs.getInt("seq");
				String title = rs.getString("title");
				String content = rs.getString("content");
				String writer = rs.getString("writer");
				int viewCount =rs.getInt("viewCount");
				String ip = rs.getString("ip");
				Timestamp writeDate = rs.getTimestamp("writeDate");
				String email = rs.getString("email");
				int id = rs.getInt("id");
				FreeBoardDTO dto = new FreeBoardDTO(seq,title,content,writer,viewCount,ip,writeDate,id,email);
				freeList.add(dto);

			}
			return freeList;
		}	
	}
	public int viewCount(int seq)throws Exception{ // 조회수 올리기
		String sql = "update FreeBoard set viewCount = viewCount + 1 where seq = ?";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				){
			pstat.setInt(1, seq);
			con.commit();
			int result = pstat.executeUpdate();
			return result;
		}
	}
	private PreparedStatement pstatContent(Connection con, int seq)throws Exception{
		String sql = "select * from FreeBoard where seq = ?";
		PreparedStatement pstat = con.prepareStatement(sql);
		pstat.setInt(1, seq);
		return pstat;}
	public FreeBoardDTO content(int seq)throws Exception { // 선택한 글 내용 불러오기
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = this.pstatContent(con, seq);
				ResultSet rs = pstat.executeQuery();
				){
			while(rs.next()) {
				seq = rs.getInt("seq");
				String title = rs.getString("title");
				String content = rs.getString("content");
				String writer = rs.getString("writer");
				int viewCount = rs.getInt("viewCount");
				String ip = rs.getString("ip");
				Timestamp writeDate = rs.getTimestamp("writeDate");
				String email = rs.getString("email");
				int id = rs.getInt("id");
				FreeBoardDTO dto = new FreeBoardDTO(seq, title, content, writer, viewCount ,ip, writeDate, id, email);
				return dto;
			}
			return null;
			
		}
	}

	public int deleteContent(int seq) throws Exception{ // 글 삭제
		String sql = "delete from FreeBoard where seq = ?";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				){
			pstat.setInt(1, seq);
			return pstat.executeUpdate();
		}
	}
	public int alterContent(String title, String content, int seq)throws Exception{//글 수정
		String sql = "update FreeBoard set title = ?, content = ? where seq = ? ";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				){
			pstat.setString(1, title);
			pstat.setString(2, content);
			pstat.setInt(3, seq);
			con.commit();
			int result = pstat.executeUpdate();
			return result;
		}
		
	}
	
	
	
	public int recordCount()throws Exception { // 글 갯수 
		String sql = "select count(*) record from FreeBoard";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();
				){
			rs.next();
			return rs.getInt("record");
		}
	}
	
	
	static int recordCountPerPage = 10;// 한 페이지에 보여줄 글 개수
	static int naviCountPerPage = 10;// 한페이지에 보여줄 페이지 번호 수
	
	public String getNavi(int currentPage)throws Exception {
		
		int recordTotalCount = this.recordCount(); // 전체 글 갯수
		
		
		// 전체 페이지 수
		int pageTotalCount = 0; 
		if(recordTotalCount % recordCountPerPage > 0) { //전체 글 갯수 % 한  페이지에 보여줄 글 갯수 -> 나머지 잇으면 한페이지 더 필요
			pageTotalCount = recordTotalCount / recordCountPerPage + 1;
		}else if(recordTotalCount % recordCountPerPage == 0) {
			pageTotalCount = recordTotalCount / recordCountPerPage;
		}
//----------------------------------------------------------------------------------
		if(currentPage < 1) {//최소 페이지 보다 작으면 최소페이지로
			currentPage = 1;
		}else if(currentPage > pageTotalCount) { // 현재페이지 번호가 전체페이지보다 크면 최대 페이지로
			currentPage = pageTotalCount;
		}//보안코드

		//내 위치의 기준으로 첫페이지와 끝페이지 알아내기
		int startNavi = (currentPage - 1) / naviCountPerPage * naviCountPerPage + 1;
		int endNavi = startNavi + (naviCountPerPage - 1);
		
		
		//ex) startNavi : 14페이지에 있다고 가정  첫페이지는 11  -> 14/10 = 1 -> 1*10 = 10 -> 10+1 = 11 ----> (currentPage/10)*10+1   // 10은 한번에 보여줄 페이지 숫자 범위
		// 10 20 30등 페이지일 경우도 있음 :     ★★(currentPage - 1)/10*10+1 -> 이 공식은 다 적용
	
		//최대페이지 번호보다 endNavi 번호가 크게 나옴 ㅠ
		if(endNavi > pageTotalCount) {
			endNavi = pageTotalCount;
		}
		
		boolean needPrev = true; // 이전버튼
		boolean needNext = true; // 다음버튼
		
		if(startNavi == 1) {
			needPrev = false;
		}
		if(endNavi == pageTotalCount) {
			needNext = false;
		}
		
		
		StringBuilder sb = new StringBuilder();
		
		if(needPrev) {
			sb.append("<a href='list.board01?currentPage="+(startNavi - 1)+"'> <이전 </a>");
		}
		for(int i = startNavi; i <= endNavi; i++) {
			sb.append("<a href='list.board01?currentPage="+i+ "'>  " + i + "  </a>");
			
		}
		if(needNext) {
			sb.append("<a href='list.board01?currentPage="+(endNavi + 1)+"'> 다음> </a>");
		}
		
		return sb.toString();
	
	}
	//1페이지 : 가장최신글 10개 -> seq 크면클수록 최신, 작성일 (중복 됨) -> 글 삭제하면 문제 seq
	
	
		private PreparedStatement pstatselectByPage(Connection con, int startNum, int endNum)throws Exception{
			String sql = "select * from (select row_number()over(order by seq desc) as rown, FreeBoard.* from FreeBoard) where rown between ? and ?";
			PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setInt(1, startNum);
			pstat.setInt(2, endNum);
			return pstat;
			
		}
	 	public List<FreeBoardDTO> selectByPage(int currentPage)throws Exception{ // 한 페이지에 보여줄 글 갯수
	 			
	 			int endNum = currentPage *recordCountPerPage;
	 			int startNum = endNum - 9;
			try(
					Connection con = this.getConnection();
					PreparedStatement pstat = this.pstatselectByPage(con, startNum, endNum);
					ResultSet rs = pstat.executeQuery();
					){
				List<FreeBoardDTO> list = new ArrayList<>();
				while(rs.next()) {
					int seq = rs.getInt("seq");
					String title = rs.getString("title");
					String content = rs.getString("content");
					String writer = rs.getString("writer");
					int viewCount = rs.getInt("viewCount");
					String ip = rs.getString("ip");
					Timestamp writeDate = rs.getTimestamp("writeDate");
					String email = rs.getString("email");
					int id = rs.getInt("id");
					FreeBoardDTO dto = new FreeBoardDTO(seq, title, content, writer, viewCount ,ip, writeDate, id, email);
					list.add(dto);
				}return list;
				
			}
			
		}
	 	
	 
	 
	 	
	 	
	 		
	 		
	 		
	 	//------------------------------------------------------------------------------------------------------------------------------------------------
	 	
	 	//검색기능
	 	
	 	private PreparedStatement pstatSelectCount(Connection con, String searchWord, String option )throws Exception {
 			String sql = "select count(*) count from FreeBoard where " + option + "like ?";
 			PreparedStatement pstat = con.prepareStatement(sql);
 			pstat.setString(1, "%"+searchWord+"%");
 			return pstat;
	 	}
	 	public int selectCount(String searchWord, String option)throws Exception{// 검색시 글 갯수
 			try(
 					Connection con = this.getConnection();
 					PreparedStatement pstat = this.pstatSelectCount(con, searchWord, option);
 					ResultSet rs = pstat.executeQuery();
 					){
 				rs.next();
 				return rs.getInt("count");
 			}
 		}
	 	
	 	private PreparedStatement pstatselectByPage(Connection con, int startNum, int endNum, String writer, String option)throws Exception{
			String sql = "select * from (select row_number()over(order by seq desc) as rown, FreeBoard.* from FreeBoard) where "+option+" =? and rown between ? and ?";
			PreparedStatement pstat = con.prepareStatement(sql);
			pstat.setString(1, writer);
			pstat.setInt(2, startNum);
			pstat.setInt(3, endNum);
			return pstat;
			
		}
	 	public List<FreeBoardDTO> selectByPage(int currentPage, String writer, String option)throws Exception{ // 한 페이지에 보여줄 검색 글 갯수
	 			
	 			int endNum = currentPage *recordCountPerPage;
	 			int startNum = endNum - 9;
			try(
					Connection con = this.getConnection();
					PreparedStatement pstat = this.pstatselectByPage(con, startNum, endNum, writer, option);
					ResultSet rs = pstat.executeQuery();
					){
				List<FreeBoardDTO> list = new ArrayList<>();
				while(rs.next()) {
					int seq = rs.getInt("seq");
					String title = rs.getString("title");
					String content = rs.getString("content");
					writer = rs.getString("writer");
					int viewCount = rs.getInt("viewCount");
					String ip = rs.getString("ip");
					Timestamp writeDate = rs.getTimestamp("writeDate");
					String email = rs.getString("email");
					int id = rs.getInt("id");
					FreeBoardDTO dto = new FreeBoardDTO(seq, title, content, writer, viewCount ,ip, writeDate, id, email);
					list.add(dto);
				}return list;
				
			}
			
		}
		public String getNaviSelect(int currentPage, String option, String searchWord)throws Exception {
			
			int recordTotalCount = this.selectCount(searchWord, option); // 전체 글 갯수
			
			
			// 전체 페이지 수
			int pageTotalCount = 0; 
			if(recordTotalCount % recordCountPerPage > 0) { //전체 글 갯수 % 한  페이지에 보여줄 글 갯수 -> 나머지 잇으면 한페이지 더 필요
				pageTotalCount = recordTotalCount / recordCountPerPage + 1;
			}else if(recordTotalCount % recordCountPerPage == 0) {
				pageTotalCount = recordTotalCount / recordCountPerPage;
			}
	//----------------------------------------------------------------------------------
			if(currentPage < 1) {//최소 페이지 보다 작으면 최소페이지로
				currentPage = 1;
			}else if(currentPage > pageTotalCount) { // 현재페이지 번호가 전체페이지보다 크면 최대 페이지로
				currentPage = pageTotalCount;
			}//보안코드

			//내 위치의 기준으로 첫페이지와 끝페이지 알아내기
			int startNavi = (currentPage - 1) / naviCountPerPage * naviCountPerPage + 1;
			int endNavi = startNavi + (naviCountPerPage - 1);
			
			
			//ex) startNavi : 14페이지에 있다고 가정  첫페이지는 11  -> 14/10 = 1 -> 1*10 = 10 -> 10+1 = 11 ----> (currentPage/10)*10+1   // 10은 한번에 보여줄 페이지 숫자 범위
			// 10 20 30등 페이지일 경우도 있음 :     ★★(currentPage - 1)/10*10+1 -> 이 공식은 다 적용
		
			//최대페이지 번호보다 endNavi 번호가 크게 나옴 ㅠ
			if(endNavi > pageTotalCount) {
				endNavi = pageTotalCount;
			}
			
			boolean needPrev = true; // 이전버튼
			boolean needNext = true; // 다음버튼
			
			if(startNavi == 1) {
				needPrev = false;
			}
			if(endNavi == pageTotalCount) {
				needNext = false;
			}
			
			
			StringBuilder sb = new StringBuilder();
			
			if(needPrev) {
				sb.append("<a href='list.board01?currentPage="+(startNavi - 1)+"'> <이전 </a>");
			}
			for(int i = startNavi; i <= endNavi; i++) {
				sb.append("<a href='list.board01?currentPage="+i+ "'>  " + i + "  </a>");
				
			}
			if(needNext) {
				sb.append("<a href='list.board01?currentPage="+(endNavi + 1)+"'> 다음> </a>");
			}
			
			return sb.toString();
		
		}
	 	
	 
	 	
	 	

}
