package com.changfubai.ibatis2Mybatis;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author changfu.bai@downjoy.com on 2018/8/21
 */
@SuppressWarnings("unchecked")
public class Ibatis2Mybatus {

    private static final String TYPE_ALIAS = "typeAlias";
    private static final String TYPE_ALIASES = "typeAliases";
    private static final String TYPE_HANDLER = "typeHandler";
    private static final String TYPE_HANDLERS = "typeHandlers";
    private static final String CALLBACK = "callback";
    private static final String HANDLER = "handler";
    private static final String SQL_MAP = "sqlMap";
    private static final String MAPPERS = "mappers";
    private static final String MAPPER = "mapper";
    private static final String SETTINGS = "settings";
    private static final String RESULT_MAP = "resultMap";
    private static final String TYPE = "type";
    private static final String CLASS = "class";
    private static final String RESULT = "result";
    private static final String JDBC_TYPE = "jdbcType";
    private static final String RESULT_CLASS = "resultClass";
    private static final String RESULT_TYPE = "resultType";
    private static final String PARAMETER_CLASS = "parameterClass";

    /**
     *
     * @param filesDir 指定文件或文件夹（文件夹不能为空，不会解析子文件夹）
     * DaoImpl文件处理入口类
     */
    public static void convertDaoImpl(String filesDir) {
        if (filesDir == null || filesDir.equals("")) {
            return;
        }
        File files = new File(filesDir);
        if (files.isDirectory()) {
            File[] allFiles = files.listFiles();
            assert allFiles != null;
            for (File daoImpl : allFiles) {
                if (daoImpl.isFile()) {
                    System.out.println(daoImpl.getAbsolutePath() + " is found, then handle");
                    try {
                        transportDaoFile(daoImpl);
                        System.out.println("file : " + daoImpl.getName() + " handle success");
                    } catch (Exception e) {
                        System.out.println(daoImpl.getAbsolutePath() + " convert fail :" + e.getMessage());
                    }
                } else {
                    System.out.println(daoImpl.getAbsolutePath() + "cannot be resolve");
                }
            }
        } else {
            System.out.println("only one file to be convert");
            try {
                transportDaoFile(files);
                System.out.println("file : " + files.getName() + " handle success");
            } catch (Exception e) {
                System.out.println(files.getAbsolutePath() + " convert fail :" + e.getMessage());
            }

        }
    }

    /**
     * 核心是用正则匹配替换需要处理的逻辑，可根据自己的需要修改
     */
    private static void transportDaoFile(File daoImpl) throws Exception {
        String srcStr1 = "org.springframework.orm.ibatis.support.SqlMapClientDaoSupport";
        String srcStr2 = "SqlMapClientDaoSupport";
        String srcStr3 = "getSqlMapClient";
        String srcStr4 = "queryForList";
        String srcStr5 = "queryForObject";
        String srcStr6 = "getSqlSessionTemplate";
        String replaceStr1 = "org.mybatis.spring.support.SqlSessionDaoSupport";
        String replaceStr2 = "SqlSessionDaoSupport";
        String replaceStr3 = "getSqlSession";
        String replaceStr4 = "selectList";
        String replaceStr5 = "selectOne";
        FileReader in = new FileReader(daoImpl);
        BufferedReader bufIn = new BufferedReader(in);

        // 内存流, 作为临时流
        CharArrayWriter tempStream = new CharArrayWriter();

        String line;
        while ((line = bufIn.readLine()) != null) {
            // 替换每行中, 符合条件的字符串
            line = line.replaceAll(srcStr1, replaceStr1)
                    .replaceAll(srcStr2, replaceStr2)
                    .replaceAll(srcStr3, replaceStr3)
                    .replaceAll(srcStr4, replaceStr4)
                    .replaceAll(srcStr5, replaceStr5)
                    .replaceAll(srcStr6, replaceStr3);
            // TODO more regex pattern 根据业务需求自行写正则表达式

            tempStream.write(line);
            // 添加换行符
            tempStream.append(System.getProperty("line.separator"));
        }

        // 关闭 输入流
        bufIn.close();

        // 将内存中的流 写入 文件
        FileWriter out = new FileWriter(daoImpl);
        tempStream.writeTo(out);
        out.close();

    }

    /**
     * @param xmlDir 指定文件或文件夹（文件夹不能为空，不会解析子文件夹）
     */
    public static void convertConfigXML(String xmlDir) {
        if (xmlDir == null || xmlDir.equals("")) {
            return;
        }
        File xmlFiles = new File(xmlDir);
        if (xmlFiles.isDirectory()) {
            File[] files = xmlFiles.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println(file.getAbsolutePath() + " will to be parse");
                    try {
                        transportConfigXml(file);
                        System.out.println("file : " + file.getName() + " handle success");
                    } catch (Exception e) {
                        System.out.println(xmlFiles.getAbsolutePath() + "  parse failed");
                        e.getStackTrace();
                    }
                } else {
                    System.out.println(file.getAbsolutePath() + " cannot be convert");
                }
            }
        } else {
            System.out.println(xmlFiles.getAbsolutePath() + " will to be parse");
            try {
                transportConfigXml(xmlFiles);
                System.out.println("file : " + xmlFiles.getName() + " handle success");
            } catch (Exception e) {
                e.getStackTrace();
                System.out.println(xmlFiles.getAbsolutePath() + "  parse failed");
            }
        }
    }

    /**
     *
     * 解析sqlMap配置文件，该方法可不做修改直接使用
     * 多余的setting标签将保留，根据业务需求自行处理
     */
    private static void transportConfigXml(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        DocumentType docType = document.getDocType();
        docType.setElementName("configuration");
        docType.setPublicID("-//mybatis.org//DTD Config 3.0//EN");
        docType.setSystemID("http://mybatis.org/dtd/mybatis-3-config.dtd");
        document.setDocType(docType);

        Element configuration = document.getRootElement();
        configuration.setQName(new QName("configuration", new Namespace(null, null), "configuration"));
        Iterator elements = configuration.nodeIterator();
        Collection<Object> cloneElements = new ArrayList<>();
        while (elements.hasNext()) {
            cloneElements.add(elements.next());
        }
        elements = cloneElements.iterator();
        LinkedList<Comment> comments = new LinkedList<>();
        while (elements.hasNext()) {
            Object ele = elements.next();
            Comment comment;
            Element next;
            try {
                comment = (Comment) ele;
                System.out.println("current node is a comment");
                configuration.remove(comment);
                comments.push(comment);
                continue;
            } catch (Exception e) {
                try {
                    next = (Element) ele;
                    System.out.println("current node is a element");
                } catch (Exception e1) {
                    System.out.println("current node is either a element or comment");
                    continue;
                }
            }
            Element nextParentEle;
            // 处理typeHandler,sqlmap,typeAlias
            switch (next.getQName().getName()) {
                case TYPE_ALIAS:
                    nextParentEle = configuration.element(new QName(TYPE_ALIASES));
                    if (null == nextParentEle) {
                        nextParentEle = DocumentHelper.createElement(new QName(TYPE_ALIASES));
                        configuration.add(nextParentEle);
                    }
                    break;
                case TYPE_HANDLER:
                    nextParentEle = configuration.element(new QName(TYPE_HANDLERS));
                    if (null == nextParentEle) {
                        nextParentEle = DocumentHelper.createElement(new QName(TYPE_HANDLERS));
                        configuration.add(nextParentEle);
                    }
                    Attribute attribute = next.attribute(CALLBACK);
                    if (attribute != null) {
                        next.remove(attribute);
                        next.addAttribute(new QName(HANDLER), attribute.getValue());
                    }
                    break;
                case SQL_MAP:
                    nextParentEle = configuration.element(new QName(MAPPERS));
                    if (null == nextParentEle) {
                        nextParentEle = DocumentHelper.createElement(new QName(MAPPERS));
                        configuration.add(nextParentEle);
                    }
                    break;
                case SETTINGS:  // 可以删除的节点
                    configuration.remove(next);
                default:
                    nextParentEle = null;
                    break;
            }
            if (nextParentEle == null) {
                System.out.println("unknown element");
                continue;
            }
            configuration.remove(next);
            if (next.getQName().getName().equals(SQL_MAP)) {
                next.setQName(new QName(MAPPER));
            }
            // 添加注释
            while (!comments.isEmpty()) {
                Comment curComment = comments.pop();
                curComment.setParent(null);
                nextParentEle.add(curComment);
            }
            nextParentEle.add(next);
            next.setParent(nextParentEle);
        }
        writeToXmlFile(file, document);
    }

    private static void writeToXmlFile(File file, Document document) throws Exception{
        OutputFormat format = OutputFormat.createPrettyPrint();//缩减型格式
        format.setEncoding("UTF-8");//设置文件内部文字的编码
        format.setExpandEmptyElements(true);
        format.setTrimText(false);
        format.setIndent(true);      // 设置是否缩进
        format.setIndent("    ");     // 以空格方式实现缩进
//        format.setNewlines(true);    // 设置是否换行

        FileWriter fw = new FileWriter(file);
        String encoding = "UTF-8";//设置文件的编码！！和format不是一回事
        OutputStreamWriter outstream = new OutputStreamWriter(new FileOutputStream(file), encoding);
        XMLWriter writer = new XMLWriter(outstream,format);
        writer.write(document);
        writer.close();
        fw.close();
    }

    /**
     * @param sqlMapXMLDirs 指定文件或文件夹（文件夹不能为空，不会解析子文件夹）
     */
    public static void convertSqlMapXML(String sqlMapXMLDirs) {
        if (sqlMapXMLDirs == null || sqlMapXMLDirs.equals("")) {
            return;
        }
        File xmlFiles = new File(sqlMapXMLDirs);
        if (xmlFiles.isDirectory()) {
            File[] files = xmlFiles.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println(file.getAbsolutePath() + " will to be parse");
                    System.out.println("file : " + file.getName() + " handle success");
                    try {
                        transportSqlMapXml(file);
                    } catch (Exception e) {
                        System.out.println(xmlFiles.getAbsolutePath() + "  parse failed");
                        e.getStackTrace();
                    }
                } else {
                    System.out.println(file.getAbsolutePath() + " cannot be convert");
                }
            }
        } else {
            System.out.println(xmlFiles.getAbsolutePath() + " will to be parse");
            try {
                transportSqlMapXml(xmlFiles);
                System.out.println("file : " + xmlFiles.getName() + " handle success");
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                System.out.println(xmlFiles.getAbsolutePath() + "  parse failed");
            }
        }
    }

    /**
     *
     * 解析根标签，替换命名空间等，该方法可不做修改
     *
     */
    private static void transportSqlMapXml(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        DocumentType docType = document.getDocType();
        docType.setElementName("mapper");
        docType.setPublicID("-//mybatis.org//DTD Mapper 3.0//EN");
        docType.setSystemID("http://mybatis.org/dtd/mybatis-3-mapper.dtd");
        document.setDocType(docType);

        Element mapper = document.getRootElement();
        mapper.setQName(new QName("mapper"));
        Iterator elements = mapper.elementIterator();
        while (elements.hasNext()) {
            Element next = null;
            try {
                next = (Element) elements.next();
            } catch (Exception e) {
                System.out.println("Element parse fail ");
            }
            if (next != null) {
                switch (next.getQName().getName()) {
                    case RESULT_MAP:
                        handleResultMapEle(next);
                        break;
                    default:
                        mapper.remove(next);
                        mapper.add(handleSqlOperatorEle(next));
                        break;
                }
            }
        }
        writeToXmlFile(file, document);
    }
    // 处理属性resultClass、parameterClass等，该方法可不做修改
    private static Element handleSqlOperatorEle(Element sqlOp) throws Exception {
        Attribute resultClass = sqlOp.attribute(RESULT_CLASS);
        if (resultClass != null) {
            sqlOp.remove(resultClass);
            sqlOp.addAttribute(new QName(RESULT_TYPE), resultClass.getValue());
        }
        Attribute parameterClass = sqlOp.attribute(PARAMETER_CLASS);
        if (parameterClass != null) {
            sqlOp.remove(parameterClass);
            sqlOp.addAttribute(new QName("parameterType"), parameterClass.getValue());
        }

        String text = handleSqlTextByPattern(sqlOp.asXML());
        sqlOp = DocumentHelper.parseText(text).getRootElement();

        sqlOp.setParent(null);
        return sqlOp;
    }

    // 将select/update/insert/delete等标签转成字符串,用正则匹配替换,需根据实际业务需求做处理
    // dynamic,in (%$name$%) 写法未处理
    // 标签类对isEqual/isNotEqual/isNull/isNotNull做了基本处理，其他的可自行添加
    private static String handleSqlTextByPattern(String sqlOpText) {
        // 替换like语句
        Pattern p1 = Pattern.compile("'%*\\$([a-zA-Z0-9.\\[\\]_]+)\\$%'");
        Matcher m1 = p1.matcher(sqlOpText);
        while (m1.find()) {
            sqlOpText = m1.replaceFirst("concat('%', #{" + m1.group(1) + "}, '%')");
            m1 = p1.matcher(sqlOpText);
        }

        // 替换#name# -> #{name}#
        Pattern p2 = Pattern.compile("#([a-zA-Z0-9,_.=\\[\\]]{2,})#");
        Matcher m2 = p2.matcher(sqlOpText);
        while (m2.find()) {
            sqlOpText = m2.replaceFirst("#{" + m2.group(1) + "}#");
            m2 = p2.matcher(sqlOpText);
        }

        // 替换in ($name$) -> 给出错误提示
        Pattern p3 = Pattern.compile("\\(\\$([a-zA-Z0-9,_.=\\[\\]]{2,})\\$\\)");
        Matcher m3 = p3.matcher(sqlOpText);
        while (m3.find()) {
            sqlOpText = m3.replaceFirst("<" + m3.group(1) + "><TODO:in 语句需自行处理/>");
            m3 = p3.matcher(sqlOpText);
        }
        // 替换<isNotNull property="name" prepend="AND"> --> <if test="name!=null"> AND
        Pattern p4 = Pattern.compile("<\\s*isNotNull\\s*property\\s*=\\s*\"([a-zA-Z0-9,_.=\\[\\]]{2,})\"\\s*prepend\\s*=\\s*\"([A-Za-z]*)\"\\s*>");
        Matcher m4 = p4.matcher(sqlOpText);
        while (m4.find()) {
            sqlOpText = m4.replaceFirst("<if test=\"" + m4.group(1) + "!=null\"> " + m4.group(2) + " ");
            m4 = p4.matcher(sqlOpText);
        }

        // 替换<isNotNull property="name"> --> <if test="name!=null">
        Pattern p5 = Pattern.compile("<\\s*isNotNull\\s*property\\s*=\\s*\"([a-zA-Z0-9,_.=\\[\\]]{2,})\"\\s*>");
        Matcher m5 = p5.matcher(sqlOpText);
        while (m5.find()) {
            sqlOpText = m5.replaceFirst("<if test=\"" + m5.group(1) + "!=null\"> ");
            m5 = p5.matcher(sqlOpText);
        }

        // 替换</isNotNull> </if>
        Pattern p6 = Pattern.compile("</\\s*isNotNull\\s*>");
        Matcher m6 = p6.matcher(sqlOpText);
        if (m6.find()) {
            sqlOpText = m6.replaceAll("</if>");
        }

        // 替换<isEqual property="name"> --> <if test="name!=null">
        Pattern p7 = Pattern.compile("<\\s*isEqual\\s*property\\s*=\\s*\"([a-zA-Z0-9,_.=\\[\\]]{2,})\"\\s*compareValue\\s*=\\s*\"([a-zA-Z0-9,_.=\\[\\]]{2,})\"\\s*>");
        Matcher m7 = p7.matcher(sqlOpText);
        while (m7.find()) {
            sqlOpText = m7.replaceFirst("<if test=\"" + m7.group(1) + "==" + m7.group(2) + "\"> ");
            m7 = p7.matcher(sqlOpText);
        }

        // 替换</isNotNull> </if>
        Pattern p8 = Pattern.compile("</\\s*isEqual\\s*>");
        Matcher m8 = p8.matcher(sqlOpText);
        if (m8.find()) {
            sqlOpText = m8.replaceAll("</if>");
        }

        return sqlOpText;
    }

    // 处理resultMap标签的属性替换和删除jdbcType,该方法可不做修改
    private static void handleResultMapEle(Element resultMap) {
        // attribute -- type -> class
        Attribute clazz = resultMap.attribute(CLASS);
        if (clazz != null) {
            resultMap.remove(clazz);
            resultMap.addAttribute(new QName(TYPE), clazz.getValue());
        }
        Iterator results = resultMap.elementIterator();
        while (results.hasNext()) {
            Element next = (Element) results.next();
            // remove jdbcType Attribute
            if (next.getQName().getName().equals(RESULT)) {
                Attribute jdbcType = next.attribute(JDBC_TYPE);
                if (jdbcType != null) {
                    next.remove(jdbcType);
                }
            }
        }

    }

    public static void main(String[] args) {
        // DaoImpl 文件处理 为空时不做处理
        String daoImplDirs = "";
        // sqlMapConfig.xml处理 为空时不做处理
        String configXMLDirs = "";
        // sqlMap.xml 处理 为空时不做处理
        String sqlMapXMLDirs = "";

        convertDaoImpl(daoImplDirs);
        convertConfigXML(configXMLDirs);
        convertSqlMapXML(sqlMapXMLDirs);
    }


}
