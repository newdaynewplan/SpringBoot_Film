package com.bxczp.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bxczp.entity.Film;
import com.bxczp.run.StartupRunner;
import com.bxczp.service.FilmService;
import com.bxczp.service.WebSiteInfoService;
import com.bxczp.util.DateUtil;
import com.bxczp.util.StringUtil;

//是RestController,会把Map自动封装成Json串 
@RestController
@RequestMapping("/admin/film")
public class FilmAdminController {
    
    @Resource
    private StartupRunner startupRunner;
    
    @Value("${imageFilePath}")
    private String imageFilePath;
    
    @Resource
    private FilmService filmService;
    
    @Resource
    private WebSiteInfoService webSiteInfoService;
    
    /**
     * 添加或修改电影信息
     * @param film
     * @param imageFile
     * @return
     * @throws Exception 
     */
    @RequestMapping("/save")
    public Map<String, Object> save(Film film, @RequestParam(name="imageFile")MultipartFile imageFile) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if(!imageFile.isEmpty()) {
            // 获取文件名
            String fileName = imageFile.getOriginalFilename();
            // 获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            String newFileName=DateUtil.getCurrentDateStr()+suffixName;
            film.setImageName(newFileName);
            FileUtils.copyInputStreamToFile(imageFile.getInputStream(), new File(imageFilePath+newFileName));
        }
        film.setPublishDate(new Date());
        filmService.save(film);
        map.put("success", true);
        startupRunner.loadData();
        return map;
    }
    
    
    @RequestMapping("/ckeditorUpload")
    public String ckeditorUpload(@RequestParam(name="upload")MultipartFile file, String CKEditorFuncNum) throws Exception {
        
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String newFileName=DateUtil.getCurrentDateStr()+suffixName;
        FileUtils.copyInputStreamToFile(file.getInputStream(), new File(imageFilePath+newFileName));
         
        StringBuffer sb=new StringBuffer();
        sb.append("<script type=\"text/javascript\">");
        sb.append("window.parent.CKEDITOR.tools.callFunction("+ CKEditorFuncNum + ",'" +"/static/filmImage/"+ newFileName + "','')");
        sb.append("</script>");
         
        return sb.toString();
    }
    
    
    @RequestMapping("/list")
    public Map<String, Object> list(Film film, @RequestParam(value="page", required=false)int page,
            @RequestParam(value="rows", required=false)int pageSize) {
        Map<String, Object> map = new HashMap<>();
        Long count = filmService.count(film);
        List<Film> filmList = filmService.list(film, page-1, pageSize);
        map.put("total", count);
        map.put("rows", filmList);
        return map;
    }
    
    @RequestMapping("/delete")
    public Map<String, Object> delete(String ids) {
        Map<String, Object> map = new HashMap<>();
        boolean flag = true;
        if (StringUtil.isNotEmpty(ids)) {
            String[] id = ids.split(",");
            for (String i : id) {
                if (webSiteInfoService.getByFilmId(Integer.parseInt(i)).isEmpty()) {
                    filmService.delete(Integer.parseInt(i));
                } else {
                    flag = false;
                }
            }
            if(flag) {
                map.put("success", true);
            } else {
                map.put("success", false);
                map.put("errorMsg", "电影动态中存在数据，电影不能删除！");
            }
        }
        startupRunner.loadData();
        return map;
    }
    
    
    @RequestMapping("/findById")
    public Film findById(String id) {
        return filmService.findById(Integer.parseInt(id));
    }
    
    @RequestMapping("/comboList")
    public List<Film> comboList(@RequestParam(value="q", required=false)String name) {
        List<Film> filmList = null;
        if (StringUtil.isNotEmpty(name)) {
            Film film = new Film();
            film.setName(name);
            filmList = filmService.list(film, 0, 30);
        }
        return filmList;
    }

}
