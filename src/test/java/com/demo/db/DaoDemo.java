/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.demo.db;

import java.io.InputStream;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class DaoDemo {

	public static void main(String[] args) throws Exception {
		// 1. 加载MyBatis的配置文件：mybatis.xml（它也加载关联的映射文件，也就是mappers结点下的映射文件）
		InputStream in = DaoDemo.class.getResourceAsStream("config.xml");

		// 2. SqlSessionFactoryBuidler实例将通过输入流调用build方法来构建 SqlSession 工厂
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);

		// 3. 通过工厂获取 SqlSession 实例，SqlSession 完全包含了面向数据库执行 SQL 命令所需的所有方法。
		SqlSession session = sqlSessionFactory.openSession();

		// 4. 准备基本信息
		// 4.1) statement: 用来定位映射文件（StudentMapper.xml）中的语句（通过namespace id + select id)
		String statement = "com.demo.db.StudentMapper.getStudent";

		// 4.2) paramter: 传进去的参数，也就是需要获取students表中主键值为1的记录
		int parameter = 1;

		// int c = System.in.read();
		// while (c != 'q') {
		// 5. SqlSession 实例来直接执行已映射的 SQL 语句，selectOne表示获取的是一条记录
		Student student = session.selectOne(statement, parameter);
		System.err.println(student);

		// c = System.in.read();
		// }

		// 6. 关闭输入流和SqlSession实例
		in.close();
		session.close();
	}
}
