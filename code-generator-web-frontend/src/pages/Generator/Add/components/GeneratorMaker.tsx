import FileUploader from '@/components/FileUploader';
import {
  downloadGeneratorByIdUsingGet,
  downloadTemplateToolUsingGet,
  makeGeneratorUsingPost
} from '@/services/backend/generatorController';
import {ProFormInstance} from '@ant-design/pro-components';
import {ProForm, ProFormItem} from '@ant-design/pro-form';
import {Alert, Button, Collapse, message, Typography} from 'antd';
import {saveAs} from 'file-saver';
import React, {useRef, useState} from 'react';
import {bool} from "prop-types";
import {DownloadOutlined} from "@ant-design/icons";
import {history} from "@@/core/history";

interface Props {
  meta: any;
}

export default (props: Props) => {
  const { meta } = props;
  const formRef = useRef<ProFormInstance>();
  const [loading, setLoading] = useState<boolean>(false);
  const [downloading, setDownloading] = useState<boolean>(false);

  /**
   * 制作
   * @param values
   */
  const doMake = async (values: API.GeneratorMakeRequest) => {
    setLoading(true);
    // console.log(meta);
    // console.log(values);
    // 数据转换
    if (!meta.name) {
      message.error('请填写名称');
      setLoading(false);
      return;
    }

    // 文件列表转 url
    const zipFilePath = values.zipFilePath;
    // console.log(zipFilePath);
    if (!zipFilePath || zipFilePath.length < 1) {
      message.error('请上传模板文件压缩包');
      setLoading(false);
      return;
    }

    // 文件列表转换成 url
    // @ts-ignore
    values.zipFilePath = zipFilePath[0].response;

    try {
      const blob = await makeGeneratorUsingPost(
        {
          meta,
          zipFilePath: values.zipFilePath,
        },
        {
          responseType: 'blob',
        },
      );
      // 使用 file-saver 来保存文件
      saveAs(blob, meta.name + '.zip');
      setLoading(false);
    } catch (error: any) {
      message.error('制作失败，' + error.message);
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    try {
      const response = await downloadTemplateToolUsingGet();
      // console.log(response);
    } catch (error: any) {
      message.error('下载失败，' + error.message);
    }
  };

  /**
   * 表单视图
   */
  const formView = (
    <ProForm
      formRef={formRef}
      loading={loading}
      submitter={{
        searchConfig: {
          submitText: '制作',
        },
        resetButtonProps: {
          hidden: true,
        },
          render: (_, dom) => (
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <div style={{ paddingLeft: '25px' }}>
                      {dom}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      <Button
                          icon={<DownloadOutlined />}
                          loading={downloading}
                          onClick={async () => {
                              setDownloading(true);
                              const blob = await downloadTemplateToolUsingGet({
                                  responseType: 'blob',
                              });
                              saveAs(blob, 'templateTool.zip');
                              setDownloading(false);
                          }}
                      >
                          下载
                      </Button>
                      <Typography.Text type="secondary" style={{ marginTop: 10, fontSize: '11px', display: 'block' }}>
                          没有模板文件?下载模板制作工具
                      </Typography.Text>
                  </div>
              </div>
          ),
      }}
      onFinish={doMake}
    >
      <ProFormItem label="模板文件" name="zipFilePath">
            <FileUploader
              biz="generator_make_template"
              description="请上传压缩包，打包时不要添加最外层目录！"
            />
      </ProFormItem>
    </ProForm>
  );

  return (
    <Collapse
      style={{
        marginBottom: 24,
      }}
      items={[
        {
          key: 'maker',
          label: '生成器制作工具',
          children: formView,
        },
      ]}
    />
  );
};
