# archives/setting.dat
* last: ``返回:String`` 最后一次存档的文件夹名称
* checksums: ``返回:ArrayMap<String, byte[]>`` 最后一次存档的所有文件的MD5值


# archives/[存档名称]/content.dat
* same: ``返回:ArrayMap<String, String>`` 与之前存档重复的所有文件相对路径(archiveDirectory路径下)
* time: ``返回:String`` 格式为yyyy-MM-dd HH:mm:ss:SSS，与存档文件夹时间名称精度相同但格式不同。
* name: ``返回:String`` 存档真实的名称(不填写时和文件夹名称相同)
