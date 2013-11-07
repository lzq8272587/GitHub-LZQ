package net.techest.socksv5;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.techest.socksv5.helper.Logger;

/*  Copyright 2010 princehaku
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on : 2011-8-20, 下午3:59:48
 *  Author     : princehaku
 */

public class SocksV5Server extends Thread {
	
	private int port;
	
	public SocksV5Server(int port){
		this.port=port;
	}

	@Override
	public void run(){
		 //创建一个socket监听本地端口
		try {
			ServerSocket sck=new ServerSocket(port);
			System.out.println("Server Started\r\nListening on port:"+port);
			do {
				// 接收一个连接
				Socket client = sck.accept();
				//TODO::处理黑名单
				Logger.getInstance().log("######Accept From "+client.getRemoteSocketAddress().toString()+"######");
				
				//从线程池加入进程执行
				//ExecutorService pool = Executors.newFixedThreadPool(33);
				Thread t=new SocksV5Handler(client);
				//pool.execute(t);
				t.start();
			} while (true);
		}  catch (IOException e) {
			System.out.println("Bind port error at port:"+port);
		}
	}
	

}
