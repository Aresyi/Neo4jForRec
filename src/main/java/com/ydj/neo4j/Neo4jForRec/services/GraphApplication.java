package com.ydj.neo4j.Neo4jForRec.services;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ydj.neo4j.Neo4jForRec.dao.GraphDao;
import com.ydj.neo4j.Neo4jForRec.dao.GraphDao.FriendOfFriend;

/**
*
* @author : Ares.yi
* @createTime : 2014-11-10 上午11:13:42 
* @version : 1.0 
* @description : 
*
 */
@EnableAutoConfiguration
@ComponentScan("com.ydj.neo4j.Neo4jForRec.*")
@RestController("/")
public class GraphApplication extends WebMvcConfigurerAdapter {

    @Resource
	private GraphDao graphDao;
	
    
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
     @RequestMapping("/addUser")
	 public int addUser(int uid,String personIUCode){
		
    	 this.graphDao.addUser(uid, personIUCode);
		 
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
	 @RequestMapping("/updateUserIUCode")
	 public int updateUserIUCode(int uid,String personIUCode){
		 
		int res = this.graphDao.updateUserIUCode(uid, personIUCode);
		 
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
	 @RequestMapping("/addFriend")
	 public int addFriend(int uid,int friendUid){
		 
		 this.graphDao.addFriend(uid, friendUid);
		 
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
	 @RequestMapping("/delFriend")
	 public int delFriend(int uid,int friendUid){
				 
		 this.graphDao.delFriend(uid, friendUid);
		 
		 return 1;
	 }
	 
    
    
    /**
     * 获取好友的好友
     * 
     * @param uid
     * @param personIUCode
     * @return
     *
     * @author : Ares.yi
     * @createTime : 2016年11月21日 下午2:50:05
     */
    @RequestMapping("/getFoF")
    public Set<String> getFriendOfFriend(@RequestParam("uid") int uid,@RequestParam("personIUCode") String personIUCode) {
    	
    	if(uid < 10000 || personIUCode == null || "".equals(personIUCode.trim())){
    		return new HashSet<String>();
    	}
    	
    	List<FriendOfFriend>  resList = this.graphDao.getFriendOfFriend(uid, personIUCode);
        
    	Set<String> res = new HashSet<String>();
    	
    	if(resList != null && resList.size() > 0){
    		for(FriendOfFriend f : resList){
    			res.add(f.uid);
    		}
    	}
    	
        return res;
    }

    /**
     * 上传通讯录
     * 
     * @param uid
     * @param mobiles
     *
     * @author : Ares.yi
     * @createTime : 2016年11月29日 上午11:47:19
     */
    @RequestMapping("/uploadMobileAddress")
    public void uploadMobileAddress(@RequestParam("uid") int uid,@RequestParam(value="mobiles[]") String[] mobiles) {
    	
    	if(uid < 10000 || mobiles == null || mobiles.length == 0 ){
    		return ;
    	}
    	
    	Set<String> set = new HashSet<String>();
    	
    	for(String one : mobiles){
    		set.add(one);
    	}
    	
    	this.graphDao.addMobileAddress(uid, set);
    	
        return ;
    }
    
    
    /**
     * 可能认识的人脉推荐
     * 
     * @param uid
     * @param oneType
     * @param twoType
     * @param threeType
     * @param fourType
     * @param oneNextPage
     * @param twoNextPage
     * @param threeNextPage
     * @param fourNextPage
     * @param pageSize
     * @return
     *
     * @author : Ares.yi
     * @createTime : 2016年11月29日 下午2:38:17
     */
    @RequestMapping("/getRec")
    public JSONObject getRec(int uid,String oneType,String twoType,String threeType,String fourType,
    		@RequestParam(defaultValue="1") int oneNextPage,@RequestParam(defaultValue="1") int twoNextPage,
    		@RequestParam(defaultValue="1") int threeNextPage,@RequestParam(defaultValue="1") int fourNextPage,int pageSize) {
    	
    	System.out.println(new Date()+"	/getRec.do?uid="+uid+"&oneNextPage="+oneNextPage+"&twoNextPage="+twoNextPage+"&threeNextPage="+threeNextPage+"&fourNextPage="+fourNextPage);
    	
    	JSONObject res = new JSONObject();
    	JSONObject param = new JSONObject();
    	
    	if( uid < 10000 || "noData".equals(fourType)){
    		param.put("fourType", "noData");
    		
    		res.put("param", param);
    		res.put("dataSize", 0);
        	res.put("data", "[]");
    		return res;
    	}
    	
    	List<JSONObject>  resList = new ArrayList<JSONObject>();
    	
    	if(!"noData".equals(oneType)){
    		List<JSONObject> oneList = this.graphDao.getMyHasMobile(uid, oneNextPage, pageSize);
            
        	if(oneList == null || oneList.size() < pageSize){//忽略最后一页正好等于pageSize情况
        		param.put("oneType", "noData");
        	}

        	param.put("oneNextPage", oneNextPage+1);
        	
        	resList.addAll(oneList);
    	}
    	
    	if(resList.size() < pageSize && !"noData".equals(twoType)){
    		List<JSONObject> twoList = this.graphDao.getOtherHasMyMobile(uid, twoNextPage, pageSize);
            
        	if(twoList == null || twoList.size() < pageSize){
        		param.put("twoType", "noData");
        	}

        	param.put("oneType", "noData");
        	
        	param.put("twoNextPage", twoNextPage+1);
        	
        	resList.addAll(twoList);
    	}
    	
    	if(resList.size() < pageSize && !"noData".equals(threeType)){
    		List<JSONObject> threeList = this.graphDao.getWeHasSameMobile(uid, threeNextPage, pageSize);
            
        	if(threeList == null || threeList.size() < pageSize){
        		param.put("threeType", "noData");
        	}

        	param.put("oneType", "noData");
        	param.put("twoType", "noData");
        	
        	param.put("threeNextPage", threeNextPage+1);
        	
        	resList.addAll(threeList);
    	}
    	
    	if(resList.size() < pageSize && !"noData".equals(fourType)){
    		List<JSONObject> fourList = this.graphDao.getTheSameFriend(uid, fourNextPage, pageSize);
            
        	if(fourList == null || fourList.size() < pageSize){
        		param.put("fourType", "noData");
        	}

        	param.put("oneType", "noData");
        	param.put("twoType", "noData");
        	param.put("threeType", "noData");
        	
        	param.put("fourNextPage", fourNextPage+1);
        	
        	resList.addAll(fourList);
    	}
    	
    	res.put("param", param);
    	res.put("dataSize", resList.size());
    	res.put("data", resList);
    	
        return res;
    }
    
    
    
    
    /**
     * Start spring-boot
     * 
     * @param args
     * @throws Exception
     *
     * @author : Ares.yi
     * @createTime : 2016年11月21日 下午2:16:14
     */
    public static void main(String[] args) throws Exception {
        System.setErr(new PrintStream(System.out) {
            @Override
            public void write(int b) {
                super.write(b);
            }

            @Override
            public void write(byte[] buf, int off, int len) {
                super.write(buf, off, len);
            }
        });
        new SpringApplicationBuilder(GraphApplication.class).run(args);
    }

}
