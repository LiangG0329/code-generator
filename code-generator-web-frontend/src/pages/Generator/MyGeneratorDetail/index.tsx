import AuthorInfo from '@/pages/Generator/Detail/components/AuthorInfo';
import FileConfig from '@/pages/Generator/Detail/components/FileConfig';
import ModelConfig from '@/pages/Generator/Detail/components/ModelConfig';
import {
    deleteGeneratorUsingPost,
    downloadGeneratorByIdUsingGet,
    getGeneratorVoByIdUsingGet,
} from '@/services/backend/generatorController';
import {Link, useModel, useParams} from '@@/exports';
import {
    CheckCircleTwoTone,
    ClockCircleTwoTone,
    DeleteOutlined,
    DownloadOutlined,
    EditOutlined, ExclamationCircleOutlined,
    ExclamationCircleTwoTone
} from '@ant-design/icons';
import {PageContainer} from '@ant-design/pro-components';
import {Button, Card, Col, Image, message, Modal, Popconfirm, Row, Space, Tabs, Tag, Typography} from 'antd';
import {history} from '@umijs/max';
import {saveAs} from 'file-saver';
import moment from 'moment';
import React, {useEffect, useState} from 'react';
import {FALLBACK_IMAGE_URL} from "@/constants";

/**
 * 生成器详情页
 * @constructor
 */
const GeneratorDetailPage: React.FC = () => {
    const {id} = useParams();

    const [loading, setLoading] = useState<boolean>(false);
    const [data, setData] = useState<API.GeneratorVO>({});
    const [downloading, setDownloading] = useState<boolean>(false);
    const {initialState} = useModel('@@initialState');
    const {currentUser} = initialState ?? {};
    const my = currentUser?.id === data?.userId;
    const canDownload = data.distPath !== null;

    /**
     * 加载数据
     */
    const loadData = async () => {
        if (!id) {
            return;
        }
        setLoading(true);
        try {
            const res = await getGeneratorVoByIdUsingGet({
                id,
            });
            setData(res.data || {});
        } catch (error: any) {
            message.error('获取数据失败，' + error.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        loadData();
    }, [id]);

    /**
     * 标签列表视图
     * @param tags
     */
    const tagListView = (tags?: string[]) => {
        if (!tags) {
            return <></>;
        }

        return (
            <div style={{marginBottom: 8}}>
                {tags.map((tag: string) => {
                    return <Tag key={tag}>{tag}</Tag>;
                })}
            </div>
        );
    };

    /**
     * 下载按钮
     */
        // const downloadButton = data.distPath && currentUser && (
    const downloadButton = (
            <Button
                icon={<DownloadOutlined/>} disabled={!canDownload}
                loading={downloading}
                onClick={async () => {
                    setDownloading(true);
                    if (!currentUser) {
                        const defaultDownloadFailureMessage = '请先登录';
                        message.error(defaultDownloadFailureMessage);
                        history.push('/user/login?redirect=/generator/detail/' + id)
                    } else {
                        const blob = await downloadGeneratorByIdUsingGet(
                            {
                                id: data.id,
                            },
                            {
                                responseType: 'blob',
                            },
                        );
                        // 使用 file-saver 来保存文件
                        const fullPath = data.distPath || '';
                        saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
                        setDownloading(false);
                    }
                }}
            >
                下载
            </Button>
        );

    /**
     * 编辑按钮
     */
        // const editButton = my && (
    const editButton = (
            <Link to={`/generator/update?id=${data.id}`}>
                <Button icon={<EditOutlined/>} disabled={!my}>编辑</Button>
            </Link>
        );

    /**
     * 删除按钮
     */
        // const editButton = my && (
    const deleteButton = (
            <Button
                danger
                icon={<DeleteOutlined/>}
                onClick={() => {
                    Modal.confirm({
                        title: '确定要删除这个项目吗?',
                        icon: <ExclamationCircleOutlined />,
                        content: '删除后将无法恢复，请谨慎操作。',
                        okText: '确定',
                        cancelText: '取消',
                        style: { top: 250 },
                        onOk: async () => {
                            {
                                try {
                                    await deleteGeneratorUsingPost({
                                        id: data.id as any,
                                    });
                                    message.success('删除成功');
                                } catch (error) {
                                    message.error('删除失败');
                                    // console.error(error);
                                    return false;
                                }
                                history.push('/generator/my');
                                return true;
                            }
                        },
                        onCancel: () => {
                            // console.log('取消');
                            return false;
                        },
                    });
                }}
            >
                删除
            </Button>
        );

    return (
        <PageContainer title={<></>} loading={loading}>
            <Card>
                <Row justify="space-between" gutter={[32, 32]}>
                    <Col flex="auto">
                        <Space size="large" align="center">
                            <Typography.Title level={4}>{data.name}</Typography.Title>
                            {tagListView(data.tags)}
                            <div style={{marginBottom: 5}}>
                                {(() => {
                                    switch (data.status) {
                                        case 0:
                                            return  <span style={{ color: 'orange' }}><ClockCircleTwoTone twoToneColor="#FFA500" /> 审核中</span>;
                                        case 1:
                                            return <span style={{ color: 'green' }}><CheckCircleTwoTone twoToneColor="#52c41a"/> 已发布</span>;
                                        case 2:
                                            return <span style={{ color: 'red' }}><ExclamationCircleTwoTone twoToneColor="#FF0000"/> 审核未通过</span>;
                                        default:
                                            return <span>未知状态</span>;
                                    }
                                })()}
                            </div>
                        </Space>
                        <Typography.Paragraph>{data.description}</Typography.Paragraph>
                        <Typography.Paragraph type="secondary">
                            创建时间：{moment(data.createTime).format('YYYY-MM-DD hh:mm:ss')}
                        </Typography.Paragraph>
                        <Typography.Paragraph type="secondary">基础包：{data.basePackage}</Typography.Paragraph>
                        <Typography.Paragraph type="secondary">版本：{data.version}</Typography.Paragraph>
                        <Typography.Paragraph type="secondary">作者：{data.author}</Typography.Paragraph>
                        {canDownload ?
                            <Typography.Paragraph type="success">下载次数：{data.downloadCount}</Typography.Paragraph> :
                            <Typography.Paragraph type="danger">暂无可下载文件资源</Typography.Paragraph>}
                        <div style={{marginBottom: 24}}/>
                        <Space size="middle">
                            <Link to={`/generator/use/${data.id}`}>
                                <Button type="primary" disabled={data.status !== 1}>立即使用</Button>
                            </Link>
                            {downloadButton}
                            {editButton}
                            {deleteButton}
                        </Space>
                    </Col>
                    <Col flex="320px">
                        <Image src={data.picture} fallback={FALLBACK_IMAGE_URL}/>
                    </Col>
                </Row>
            </Card>
            <div style={{marginBottom: 24}}/>
            <Card>
                <Tabs
                    size="large"
                    defaultActiveKey={'fileConfig'}
                    onChange={() => {
                    }}
                    items={[
                        {
                            key: 'fileConfig',
                            label: '文件配置',
                            children: <FileConfig data={data}/>,
                        },
                        {
                            key: 'modelConfig',
                            label: '模型配置',
                            children: <ModelConfig data={data}/>,
                        },
                        {
                            key: 'userInfo',
                            label: '作者信息',
                            children: <AuthorInfo data={data}/>,
                        },
                    ]}
                />
            </Card>
        </PageContainer>
    );
};

export default GeneratorDetailPage;
