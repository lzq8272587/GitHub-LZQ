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
 *  Created on : 2011-8-21, 上午9:31:21
 *  Author     : princehaku
 */

package net.techest.socksv5.helper;

public class ByteHelper {
		/**将一个byte转换为String的ip地址
		 * 
		 * @param addr
		 * @param offset
		 * @return
		 */
	   public static final String bytes2IPV4(byte[] addr,int offset){   
		      String hostName = ""+(addr[offset] & 0xFF);   
		      for(int i = offset+1;i<offset+4;++i)   
		        hostName+="."+(addr[i] & 0xFF);   
		      return hostName;   
		   }   
}
