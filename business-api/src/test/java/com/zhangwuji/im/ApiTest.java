//package com.zhangwuji.im;
//
//import static org.junit.Assert.*;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockHttpSession;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//public class ApiTest  extends TApplicationTests{
//	 @Autowired
//	 private WebApplicationContext wac;
//	 
//	 private MockMvc mvc;
//	 private MockHttpSession session;
//	 
//	@Test
//	public void test() {
//		fail("Not yet implementd");
//	}
//	
//	 @Test
//	  public void listUserTest() throws Exception{
//	        String json="1";
//	        mvc.perform(MockMvcRequestBuilders.get("/api/users/1/1")
//	                .accept(MediaType.APPLICATION_JSON_UTF8)
//	                .content(json.getBytes())//传json参数
//	                .session(session)
//	        )
//	                .andExpect(MockMvcResultMatchers.status().isOk())
//	                .andDo(MockMvcResultHandlers.print());
//
//	  }
//	 
//	 @Before
//	 public void setupMockMvc(){
//	     mvc = MockMvcBuilders.webAppContextSetup(wac).build(); //初始化MockMvc对象
//	     session = new MockHttpSession();
//	     session.setAttribute("user","true"); 
//	 }
//	 
//	 @Test
//	 public void userLoginTest() throws Exception {
//		 String json="appId=888888&username=test&password=123123";
//	     mvc.perform(MockMvcRequestBuilders.post("/api/checkLogin")
//	                 .contentType(MediaType.APPLICATION_JSON_UTF8)
//	                 .content(json.getBytes())//传json参数
//	                 .header("token", "")
//	                 .header("appId", "88888")
//	                 .accept(MediaType.APPLICATION_JSON_UTF8)
//	                 .session(session)
//	         )
//	        .andExpect(MockMvcResultMatchers.status().isOk())
//	       // .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"))
//	        .andDo(MockMvcResultHandlers.print());
//	 }
//}
