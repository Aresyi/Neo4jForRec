package com.ydj.neo4j.Neo4jForRec.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

/**  
 *
 * @author : Ares.yi
 * @createTime : 2014-11-10 上午11:13:42 
 * @version : 1.0 
 * @description : 
 *
 */
@Repository("graphDao")
public class GraphDao {

	 @Autowired
	 JdbcTemplate template;
	 
	 @Bean
	 public javax.sql.DataSource dataSource() {
	     String NEO4J_URL = System.getenv("NEO4J_URL");
	     if (NEO4J_URL==null) NEO4J_URL=System.getProperty("NEO4J_URL","jdbc:neo4j:http://192.168.254.9:7474");//bolt://localhost:7687
//	     if (NEO4J_URL==null) NEO4J_URL=System.getProperty("NEO4J_URL","bolt://192.168.254.9:7687");//bolt://localhost:7687
	     DriverManagerDataSource ds = new DriverManagerDataSource(NEO4J_URL);
	     ds.setUsername("neo4j");
	     ds.setPassword("123456");
	     return ds;
	 }
	 
	 public class FriendOfFriend {
	        public String uid;
	        public int count;

	        public FriendOfFriend(String uid, int count) {
	            this.uid = uid;
	            this.count = count;
	        }
	 }
	 
	 private final RowMapper<FriendOfFriend> FOF_ROW_MAPPER = new RowMapper<FriendOfFriend>() {
	        public FriendOfFriend mapRow(ResultSet rs, int rowNum) throws SQLException {
	            return new FriendOfFriend(rs.getString("uid"),rs.getInt("count"));
	        }
	 };
	 
	 
	 private final RowMapper<JSONObject> JSON_ROW_MAPPER = new RowMapper<JSONObject>() {
	        public JSONObject mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	
	    		JSONObject json = new JSONObject();
	    		ResultSetMetaData rsmd = rs.getMetaData();
	    		int columnCount = rsmd.getColumnCount();

	    		for (int index = 1; index <= columnCount; index++) {
	    			String column = JdbcUtils.lookupColumnName(rsmd, index);
	    			Object value = rs.getObject(column);
	    		
	    			json.put(column, "null".equalsIgnoreCase(value.toString()) ? "": value);
	    			
	    		}
	    		
	    		return json;
	        }
	 };
	 
	 
	 
	 
	 /**
	  * 添加新用户
	  * 
	  * @param uid
	  * @param personIUCode
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月21日 下午3:03:26
	  */
	 public int addUser(int uid,String personIUCode){
		 String sql = "MERGE (n:Person {uid:'%d', personIUCode:'%s'})";
		 
		 sql = String.format(sql, uid,personIUCode);
		 
		 this.template.execute(sql);
		 
		 return 1;
	 }
	 
	 
	 /**
	  * 修改用户行业
	  * 
	  * @param uid
	  * @param personIUCode
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月21日 下午3:03:26
	  */
	 public int updateUserIUCode(int uid,String personIUCode){
		 String sql = "Match (u:Person {uid:'%d'}) set u.personIUCode='%s' return u";
		 
		 sql = String.format(sql, uid,personIUCode);
		 
		 int res = this.template.update(sql);
		 
		 return res;
	 }
	 
	 
	 /**
	  * 添加新好友关系
	  * 
	  * @param uid
	  * @param friendUid
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月21日 下午3:08:03
	  */
	 public int addFriend(int uid,int friendUid){
				 
		 String sql = "Match(a:Person {uid:'%d'}) WITH(a) Match(b:Person {uid:'%d'}) WITH b,a MERGE (a)-[:F]->(b) MERGE (b)-[:F]->(a)  return a,b ";
		 
		 sql = String.format(sql, uid,friendUid);
		 
		 this.template.execute(sql);
		 
		 return 1;
	 }
	 
	 
	 /**
	  * 删除好友关系
	  * 
	  * @param uid
	  * @param friendUid
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月21日 下午3:13:26
	  */
	 public int delFriend(int uid,int friendUid){
				 
		 String sql = "Match (a:Person {uid:'%d'})-[r:F]-(b:Person {uid:'%d'}) delete  r";
		 
		 sql = String.format(sql, uid,friendUid);
		 
		 this.template.execute(sql);
		 
		 return 1;
	 }
	 
	 
	 /**
	  * 获取好友的好友
	  * @param uid
	  * @param personIUCode
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月21日 上午10:12:21
	  */
	 public List<FriendOfFriend> getFriendOfFriend(int uid,String personIUCode) {
	    	String sql = "Match (user:Person {uid:{1}}) match (user)-[:F]->(friend:Person {personIUCode:{2}})-[:F]->(fof:Person {personIUCode:{2}}) with user,fof OPTIONAL Match (user)-[r]-(fof) with fof,r where r is null  RETURN fof.uid as uid, COUNT(*) as count ORDER BY COUNT(*) DESC, fof.uid limit 20";
	    	List<FriendOfFriend>  res = template.query(sql, new Object[]{uid+"",personIUCode},FOF_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 
	 /**
	  * 添加手机通讯录
	  * 
	  * @param uid
	  * @param mobiles
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月28日 下午7:09:01
	  */
	 public void addMobileAddress(int uid,Set<String> mobiles){
		 StringBuffer sql = new StringBuffer("MERGE (p:Person {uid:"+uid+"}) ");
		
		 String oneMobile = "MERGE (m%s:M {num:'%s'}) ";
		 String oneHas = "MERGE (p)-[:H]->(m%s) ";
		 
		 for(String mobile : mobiles){
			 
			 if(!Tools.isEmail(mobile) && !Tools.isMobile(mobile)){
				 continue;
			 }
			 
			 sql.append(String.format(oneMobile, mobile,mobile))
			 	.append(String.format(oneHas, mobile));
		 }
		 
		 
		 System.out.println(sql.toString());
		 
		 this.template.execute(sql.toString());
	 }
	 
	 
	/**
	 * 我的通讯录联系人在人脉通中注册，但和我还不是好友（我有他的手机号码）
	 * 
	 * @param uid
	 * @param page
	 * @param pageSize
	 * @return
	 *
	 * @author : Ares.yi
	 * @createTime : 2016年11月29日 上午10:17:40
	 */
	 public List<JSONObject> getMyHasMobile(int uid,int page ,int pageSize) {
	    	String sql = "MATCH (a:Person {uid:{1}})-[:H]->(m)-[:Belong]->(b:Person) WHERE NOT (a)-[:F]-(b) RETURN b.uid AS uid,1 AS type SKIP {2} LIMIT {3}";
	    	
	    	int skip = (page - 1) * pageSize < 0 ? 0 : (page - 1) * pageSize ; 
	    	
	    	List<JSONObject>  res = template.query(sql, new Object[]{uid+"",skip,pageSize},JSON_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 /**
	  * 拥有我的号码，但不是好友（他有我的手机号码）
	  * 
	  * @param uid
	  * @param page
	  * @param pageSize
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月29日 上午10:32:31
	  */
	 public List<JSONObject> getOtherHasMyMobile(int uid,int page ,int pageSize) {
	    	String sql = "MATCH (a:Person {uid:{1}})<-[:Belong]-(m)<-[:H]-(b:Person) WHERE NOT (a)-[:F]-(b) RETURN b.uid AS uid,2 AS type SKIP {2} LIMIT {3}";
	    	
	    	int skip = (page - 1) * pageSize < 0 ? 0 : (page - 1) * pageSize ; 
	    	
	    	List<JSONObject>  res = template.query(sql, new Object[]{uid+"",skip,pageSize},JSON_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 
	 /**
	  * 可能认识（我们都有同一个手机号码）
	  * 
	  * @param uid
	  * @param page
	  * @param pageSize
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月29日 上午10:34:40
	  */
	 public List<JSONObject> getWeHasSameMobile(int uid,int page ,int pageSize) {
	    	String sql = "MATCH (a:Person {uid:{1}})-[:H]->(m)<-[:H]-(b:Person) WHERE NOT (a)-[:F]-(b) RETURN DISTINCT b.uid AS uid,3 AS type SKIP {2} LIMIT {3}";
	    	
	    	int skip = (page - 1) * pageSize < 0 ? 0 : (page - 1) * pageSize ; 
	    	
	    	List<JSONObject>  res = template.query(sql, new Object[]{uid+"",skip,pageSize},JSON_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 
	 /**
	  * 有N个共同好友
	  * 
	  * @param uid
	  * @param page
	  * @param pageSize
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月29日 上午10:45:32
	  */
	 public List<JSONObject> getTheSameFriend(int uid,int page ,int pageSize) {
	    	String sql = "MATCH (a:Person {uid:{1}})-[:F]->(friend)<-[:F]-(b:Person) RETURN b.uid AS uid,COUNT(*) AS comCount,4 AS type ORDER BY comCount DESC SKIP {2} LIMIT {3}";
	    	
	    	int skip = (page - 1) * pageSize < 0 ? 0 : (page - 1) * pageSize ; 
	    	
	    	List<JSONObject>  res = template.query(sql, new Object[]{uid+"",skip,pageSize},JSON_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 /**
	  * 共同好友列表
	  * 
	  * @param uid1
	  * @param uid2
	  * @param page
	  * @param pageSize
	  * @return
	  *
	  * @author : Ares.yi
	  * @createTime : 2016年11月29日 上午10:55:12
	  */
	 public List<JSONObject> getSameFriend(int uid1,int uid2, int page ,int pageSize) {
	    	String sql = "MATCH (a:Person {uid:{1}})-[:F]->(friend)<-[:F]-(b:Person {uid:{2}}) RETURN friend.uid AS uid SKIP {3} LIMIT {4}";
	    	
	    	int skip = (page - 1) * pageSize < 0 ? 0 : (page - 1) * pageSize ; 
	    	
	    	List<JSONObject>  res = template.query(sql, new Object[]{uid1+"",uid2+"",skip,pageSize},JSON_ROW_MAPPER);
	        
	        return res;
	 }
	 
	 
	 public static void main(String[] args) {
		Set<String> ss = new HashSet<String>();
		ss.add("21234562");
		ss.add("e3123456");
		
		GraphDao obj = new GraphDao();
		obj.addMobileAddress(123, ss);
		
		
		System.out.println(ss.toString());
	}
}
