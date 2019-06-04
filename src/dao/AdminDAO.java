package dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import controllers.AdminController;
import dto.AdminDTO;

public class AdminDAO extends TimerTask {
	public void run() {
		try {
			insertVisitCount();
			System.out.println("DB에 저장 성공~!");
			AdminController.visitCount = 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String user = "kh";
		String pw = "kh";
		return DriverManager.getConnection(url, user, pw);
	}

	public int insertVisitCount() throws Exception{
		String sql = "insert into visit(visitCount) values(?)";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				){
			pstat.setInt(1, AdminController.visitCount);
			int result = pstat.executeUpdate();
			con.commit();
			return result;
		}
	}

	public List<AdminDTO> visitChart() throws Exception{
		String sql = "select * from visit where visitCount>1";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);		
				ResultSet rs = pstat.executeQuery();
				){
			List<AdminDTO> vList = new ArrayList<>();
			while(rs.next()) {
				String visitDate = rs.getString("visitDate");
				int visitCount = rs.getInt("visitCount");
				AdminDTO vdto = new AdminDTO(visitDate, visitCount);
				vList.add(vdto);
				con.commit();
			}
			return vList;
		}
	}

	public AdminDTO genderChart() throws Exception{
		String sql = "select sum(decode(gender, 'M', 1)) male, sum(decode(gender, 'W', 1)) female from members";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);		
				ResultSet rs = pstat.executeQuery();
				){
			AdminDTO vdto = new AdminDTO();
			while(rs.next()) {
				int male = rs.getInt("male");
				int female = rs.getInt("female");
				vdto = new AdminDTO(male, female);
				con.commit();
			}
			return vdto;
		}
	}

	public AdminDTO ageChart() throws Exception{
		String sql = "select nvl(sum(decode(age, '10-19', 1)),0) teenage, nvl(sum(decode(age, '20-29', 1)),0) twenty, nvl(sum(decode(age, '30-39', 1)),0)"
				+ " thirty, nvl(sum(decode(age, '40-49', 1)),0) forty, nvl(sum(decode(age, '50-59', 1)),0) fifty, nvl(sum(decode(age, '60-69', 1)),0) sixty,  "
				+ "nvl(sum(decode(age, '70-79', 1)),0) seventy, nvl(sum(decode(age, '80-89', 1)),0) eighty, nvl(sum(decode(age, '90-99', 1)),0) ninety from members";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);		
				ResultSet rs = pstat.executeQuery();
				){
			AdminDTO agedto = new AdminDTO();
			while(rs.next()) {
				int teenage = rs.getInt("teenage");
				int twenty = rs.getInt("twenty");
				int thirty = rs.getInt("thirty");
				int forty = rs.getInt("forty");
				int fifty = rs.getInt("fifty");
				int sixty = rs.getInt("sixty");
				int seventy = rs.getInt("seventy");
				int eighty = rs.getInt("eighty");
				int ninety = rs.getInt("ninety");
				agedto = new AdminDTO(teenage, twenty, thirty, forty, fifty, sixty, seventy, eighty, ninety);
				con.commit();
			}
			return agedto;
		}
	}

	public AdminDTO agePerChart() throws Exception{
		String sql = "select Round(max(sum(nvl(decode(age, '10-19', 1),0)))/max(count(age))*100,0) perTeenage, Round(max(sum(nvl(decode(age, '20-29', 1),0)))/max(count(age))*100,0) perTwenty, "
				+ "Round(max(sum(nvl(decode(age, '30-39', 1),0)))/max(count(age))*100,0) perThirty, Round(max(sum(nvl(decode(age, '40-49', 1),0)))/max(count(age))*100,0) perForty, "
				+ "Round(max(sum(nvl(decode(age, '50-59', 1),0)))/max(count(age))*100,0) perFifty, Round(max(sum(nvl(decode(age, '60-69', 1),0)))/max(count(age))*100,0) perSixty, "
				+ "Round(max(sum(nvl(decode(age, '70-79', 1),0)))/max(count(age))*100,0) perSeventy, Round(max(sum(nvl(decode(age, '80-89', 1),0)))/max(count(age))*100,0) perEighty, "
				+ "Round(max(sum(nvl(decode(age, '90-99', 1),0)))/max(count(age))*100,0) perNinety from members group by age";
		try(
				Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);		
				ResultSet rs = pstat.executeQuery();
				){
			AdminDTO agePerdto = new AdminDTO();
			while(rs.next()) {
				int perTeenage = rs.getInt("perTeenage");
				int perTwenty = rs.getInt("perTwenty");
				int perThirty = rs.getInt("perThirty");
				int perForty = rs.getInt("perForty");
				int perFifty = rs.getInt("perFifty");
				int perSixty = rs.getInt("perSixty");
				int perSeventy = rs.getInt("perSeventy");
				int perEighty = rs.getInt("perEighty");
				int perNinety = rs.getInt("perNinety");
				agePerdto = new AdminDTO(0, perTeenage, perTwenty, perThirty, perForty, perFifty, perSixty, perSeventy, perEighty, perNinety);
				con.commit();
			}
			return agePerdto;
		}
	}
}



