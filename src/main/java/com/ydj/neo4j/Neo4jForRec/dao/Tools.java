package com.ydj.neo4j.Neo4jForRec.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**  
 *
 * @author : Ares.yi
 * @createTime : 2014-11-10 上午11:13:42 
 * @version : 1.0 
 * @description : 
 *
 */
public class Tools {
	
	/**
	 * 判断是否为空字符串
	 * 
	 * @param src
	 * @return
	 */
	public static boolean isEmptyString(String src) {

		return src == null || src.trim().length() < 1;

	}
	
	public static boolean isNotEmpty(String src){
		return !isEmptyString(src);
	}

	/**
	 * 正则验证
	 * 
	 * @param regex
	 * @param src
	 * @param match
	 *            true为全匹配， false为包含
	 * @return
	 */
	public static boolean regex(String regex, String src, boolean match) {

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(src);

		if (match)
			return matcher.matches();

		return matcher.find();
	}
	
	
	/**
	 * 判断是否为手机号
	 * 
	 * @param mobile
	 * @return
	 */
	public static boolean isMobile(String mobile) {

		return isEmptyString(mobile) ? false : regex(
				"^(\\+86(\\s)?)?0?1(3|4|5|7|8)\\d{9}$", mobile, true);

	}
	
	
	/**
	 * 判断Email (Email由帐号@域名组成，格式为xxx@xxx.xx)<br>
	 * 帐号由英文字母、数字、点、减号和下划线组成，<br>
	 * 只能以英文字母、数字、减号或下划线开头和结束。<br>
	 * 域名由英文字母、数字、减号、点组成<br>
	 * www.net.cn的注册规则为：只提供英文字母、数字、减号。减号不能用作开头和结尾。(中文域名使用太少，暂不考虑)<br>
	 * 实际查询时-12.com已被注册。<br>
	 * 以下是几大邮箱极限数据测试结果<br>
	 * 163.com为字母或数字开头和结束。<br>
	 * hotmail.com为字母开头，字母、数字、减号或下划线结束。<br>
	 * live.cn为字母、数字、减号或下划线开头和结束。hotmail.com和live.cn不允许有连续的句号。
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {

		return isEmptyString(email) ? false
				: regex("^[\\w_-]+([\\.\\w_-]*[\\w_-]+)?@[\\w-]+\\.[a-zA-Z]+(\\.[a-zA-Z]+)?$",
								email, true);
	}
}
