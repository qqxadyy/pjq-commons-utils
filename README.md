个人整理的工具类

注意：
1.本包有依赖bouncycastle的bcpkix-jdk18on以实现国密相关方法，如果应用有引入本包且其它包有依赖bcpkix的其它版本，则建议排除其它版本的引入，例如
```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
			<exclusions>
				<exclusion>
					<!-- 移除jdk15on的依赖 -->
					<groupId>org.bouncycastle</groupId>
					<artifactId>bcpkix-jdk15on</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
```
