package us.ihmc.cd

import net.schmizz.sshj.SSHClient

open class SFTPExtension
{
   fun startSession(address: String, username: String, password: String)
   {
      val ssh = SSHClient()
      ssh.loadKnownHosts()
      ssh.connect(address)

//      try 
   }
}