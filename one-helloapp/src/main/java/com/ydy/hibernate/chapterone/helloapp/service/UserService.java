package com.ydy.hibernate.chapterone.helloapp.service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.ydy.hibernate.chapterone.helloapp.model.User;



public class UserService {
	
	public static SessionFactory sessionFactory;
	
	/**初始化Hibernate，创建SessionFactory实例*/
	static{
		try {
			//根据Hibernate配置文件信息，创建一个Configuration对象
				Configuration config = new Configuration();
			//加载User类的对象-关系映射文件
				config.addClass(User.class);
			//创建SessionFactory实例
				sessionFactory=config.buildSessionFactory();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	/**查询所有User对象，然后调用printUser()方法，打印User对象信息*/
	public void findAllUsers(ServletContext context,PrintWriter out) throws Exception{
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		tx = session.beginTransaction();    //开始一个事务
		Query query = session.createQuery("from User as u order by u.name asc");
		@SuppressWarnings("unchecked")
		List<User> users = query.list();
		for(Iterator<User> it = users.iterator();it.hasNext();){
			printUser(context, out, (User)it.next());
		}
		tx.commit();
	}
	
	/**按照id加载一个User对象，然后修改他的属性*/
	public void loadAndUpdateUser(Long user_id ,String address){
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User u = (User)session.get(User.class, user_id);
			u.setAddress(address);
			tx.commit();
		} catch (RuntimeException e) {
			if(tx!=null){
				tx.rollback();
			}
			throw e;
		}finally{
			session.close();
		}
	}
	
	/**持久化一个User对象*/
	public void saveUser(User user){
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(user);
			tx.commit();
		} catch (RuntimeException e) {
			if(tx!=null){
				tx.rollback();
			}
			throw e;
		}finally{
			session.close();
		}
	}
	
	/**删除user对象*/
	public void deleteUser(User user){
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(user);
			tx.commit();
		} catch (RuntimeException e) {
			if(tx!=null){
				tx.rollback();
			}
			throw e;
		}finally{
			session.close();
		}
	}
	
	/**选择项控制台还是web网页输出user对象信息*/
	private void printUser(ServletContext context,PrintWriter out,User user) 
			throws Exception{
		if(context != null){
			printUserInWeb(context,out,user);
		}else{
			printUser(out,user);
		}
	}
	
	/**
	 * 把User对象信息输出到控制台
	 * @throws Exception 
	 * */
	private void printUser(PrintWriter out,User user) throws Exception{
		byte[] buffer = user.getImage();
		FileOutputStream fout = new FileOutputStream("photo_copy.gif");
		fout.write(buffer);
		fout.close();
		
		out.println("-----------以下是"+user.getName()+"的个人信息--------------");
		out.println("ID: "+user.getId());
		out.println("密码： "+user.getPassword());
		out.println("姓名： "+user.getName());
		out.println("邮箱： "+user.getEmail());
		out.println("电话： "+user.getPhone());
		out.println("地址： "+user.getAddress());
		String sex = user.getSex()=='M'?"男":"女";
		out.println("性别： "+sex);
		String marriedStatus = user.isMarried()?"已婚":"未婚";
		out.println("婚姻状况： "+marriedStatus);
		out.println("简介： "+user.getDescription());
	}

	/**
	 * 把User对象信息输出到动态网页
	 * @throws Exception 
	 * */
	private void printUserInWeb(ServletContext context, PrintWriter out,
			User user) throws Exception {
		byte[] buffer = user.getImage();
		String path = context.getRealPath("/");
		FileOutputStream fout = new FileOutputStream(path+"photo_copy.gif");
		fout.write(buffer);
		fout.close();
		
		out.println("-----------以下是"+user.getName()+"的个人信息--------------"+"<br>");
		out.println("ID: "+user.getId()+"<br>");
		out.println("密码： "+user.getPassword()+"<br>");
		out.println("姓名： "+user.getName()+"<br>");
		out.println("邮箱： "+user.getEmail()+"<br>");
		out.println("电话： "+user.getPhone()+"<br>");
		out.println("地址： "+user.getAddress()+"<br>");
		String sex = user.getSex()=='M'?"男":"女";
		out.println("性别： "+sex+"<br>");
		String marriedStatus = user.isMarried()?"已婚":"未婚";
		out.println("婚姻状况： "+marriedStatus+"<br>");
		out.println("简介： "+user.getDescription()+"<br>");
		out.println("<img src='photo_copy.gif' border=0><p>");
	}
	
	public void test(ServletContext context,PrintWriter out) throws Exception{
		User user  = new User();
		user.setName("Milo");
		user.setEmail("m.ydy@foxmail.com");
		user.setPassword("1234");
		user.setPhone(56565656);
		user.setAddress("KunMing");
		user.setSex('M');
		user.setDescription("I am a good man.");
		//设置对象的image属性，它是字节数组，存放photo.gif文件的二进制数据
		//photo.gif文件和UserService.class文件位于同一个目录下
		InputStream in = this.getClass().getResourceAsStream("photo.gif");
		byte[] buffer = new byte[in.available()];
		in.read(buffer);
		user.setImage(buffer);
		//设置User对象的birthday属性，它是java.sql.Date类型
		user.setBirthday(Date.valueOf("1990-06-07"));
		
		saveUser(user);
		findAllUsers(context, out);
		loadAndUpdateUser(user.getId(), "ChengDu");
		findAllUsers(context, out);
		deleteUser(user);
		
	}
	
	public static void main(String[] args) throws Exception {
		new UserService().test(null, new PrintWriter(System.out,true));
		sessionFactory.close();
	}
	
}
