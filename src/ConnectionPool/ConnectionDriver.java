package ConnectionPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

public class ConnectionDriver {
	static class ConnectionHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// TODO Auto-generated method stub
			if (method.getName().equals("commit")) {
				TimeUnit.MILLISECONDS.sleep(100);
			}
			return null;
		}
	}
	// 创建一个简单的Connection代理
	public static final Connection createConnection() {
		return (Connection) Proxy.newProxyInstance(
				ConnectionDriver.class.getClassLoader(),
				new Class<?>[] { Connection.class }, new ConnectionHandler());
	}
}
