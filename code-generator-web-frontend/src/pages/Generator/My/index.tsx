import {listMyGeneratorVoByPageUsingPost} from '@/services/backend/generatorController';
import {
  CheckCircleTwoTone,
  ClockCircleTwoTone,
  ExclamationCircleTwoTone,
  FireTwoTone,
  UserOutlined
} from '@ant-design/icons';
import {PageContainer, ProFormSelect, ProFormText, QueryFilter} from '@ant-design/pro-components';
import {Avatar, Card, Flex, Image, Input, List, message, Tabs, Tag, Typography} from 'antd';
import moment from 'moment';
import React, {useEffect, useState} from 'react';

import {FALLBACK_IMAGE_URL} from "@/constants";
import {Link} from "umi";


/**
 * 默认分页参数
 */
const DEFAULT_PAGE_PARAMS: PageRequest = {
    current: 1,
    pageSize: 4,
    sortField: 'createTime',
    sortOrder: 'descend',
};

/**
 * 主页
 * @constructor
 */
const IndexPage: React.FC = () => {
    const [loading, setLoading] = useState<boolean>(true);
    const [dataList, setDataList] = useState<API.GeneratorVO[]>([]);
    const [total, setTotal] = useState<number>(0);
    // 搜索条件
    const [searchParams, setSearchParams] = useState<API.GeneratorQueryRequest>({
        ...DEFAULT_PAGE_PARAMS,
    });

    /**
     * 搜索
     */
    const doSearch = async () => {
        setLoading(true);
        try {
            const res = await listMyGeneratorVoByPageUsingPost(searchParams);
            setDataList(res.data?.records ?? []);
            setTotal(Number(res.data?.total) ?? 0);
        } catch (error: any) {
            message.error('获取数据失败，' + error.message);
        }
        setLoading(false);
    };

    /**
     * searchParams变化，则执行搜索
     */
    useEffect(() => {
        doSearch();
    }, [searchParams]);

    /**
     * 标签列表
     * @param tags
     */
    const tagListView = (tags?: string[]) => {
        if (!tags) {
            return <></>;
        }

        return (
            <div style={{marginBottom: 8}}>
                {tags.map((tag) => (
                    <Tag key={tag}>{tag}</Tag>
                ))}
            </div>
        );
    };

    return (
        <PageContainer title={<></>}>
            <Flex justify="center">
                <Input.Search
                    style={{
                        width: '40vw',
                        minWidth: 320,
                    }}
                    placeholder="搜索我的生成器"
                    allowClear
                    enterButton="搜索"
                    size="large"
                    onChange={(e) => {
                        searchParams.searchText = e.target.value;
                    }}
                    onSearch={(value: string) => {
                        setSearchParams({
                            ...DEFAULT_PAGE_PARAMS,
                            ...searchParams,
                            searchText: value,
                        });
                    }}
                />
            </Flex>

            <div style={{marginBottom: 16}}/>

            <Tabs
                size="large"
                defaultActiveKey="newest"
                items={[
                    {
                        key: 'newest',
                        label: '最近更新',
                    },
                    {
                        key: 'recommend',
                        label: '最多下载',
                    },
                ]}
                onChange={(activeKey) => {
                    switch (activeKey) {
                        case 'newest':
                            setSearchParams({
                                ...DEFAULT_PAGE_PARAMS,
                                ...searchParams,
                                sortField:'updateTime'
                            });
                            break;
                        case 'recommend':
                            setSearchParams({
                                ...DEFAULT_PAGE_PARAMS,
                                ...searchParams,
                                sortField:'downloadCount'
                            });
                            break;
                        default:
                            break;
                    }
                }}
            />

            <QueryFilter
                span={12}
                labelWidth="auto"
                labelAlign="left"
                defaultCollapsed={false}
                style={{padding: '16px 0'}}
                onFinish={async (values: API.GeneratorQueryRequest) => {
                    setSearchParams({
                        ...DEFAULT_PAGE_PARAMS,
                        ...searchParams,
                        // @ts-ignore
                        ...values,
                        searchText: searchParams.searchText,
                    });
                }}
            >
                <ProFormSelect label="标签" name="tags" mode="tags"/>
                <ProFormText label="名称" name="name"/>
                <ProFormText label="描述" name="description"/>
                <ProFormText label="作者" name="author"/>
            </QueryFilter>

            <div style={{marginBottom: 24}}/>

            <List<API.GeneratorVO>
                rowKey="id"
                loading={loading}
                grid={{
                    gutter: 16,
                    xs: 1,
                    sm: 2,
                    md: 3,
                    lg: 3,
                    xl: 4,
                    xxl: 4,
                }}
                dataSource={dataList}
                pagination={{
                    current: searchParams.current,
                    pageSize: searchParams.pageSize,
                    total,
                    onChange(current: number, pageSize: number) {
                        setSearchParams({
                            ...searchParams,
                            current,
                            pageSize,
                        });
                    },
                }}
                renderItem={(data) => (
                    <List.Item>
                        <Link to={`/generator/detail/my/${data.id}`}>
                            <Card hoverable
                                  cover={<Image alt={data.name} src={data.picture} fallback={FALLBACK_IMAGE_URL}
                                                style={{height: 200, objectFit: 'cover'}}/>}
                                  style={{height: '420px'}}>
                                <Card.Meta
                                    title={<a>{data.name}</a>}
                                    description={
                                        <Typography.Paragraph ellipsis={{rows: 2}} style={{height: 50}}>
                                            {data.description}
                                        </Typography.Paragraph>
                                    }
                                />
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
                                <div style={{minHeight: '30px'}}>
                                    {tagListView(data.tags)}
                                </div>
                                <Flex justify="space-between" align="center">
                                    <Typography.Text type="secondary" style={{fontSize: 12}}>
                                        {moment(data.updateTime).fromNow()}
                                    </Typography.Text>
                                  <Typography.Text type="secondary" style={{fontSize: 12}}>
                                    {data.downloadCount && parseInt(data.downloadCount) > 5 && <FireTwoTone twoToneColor="#FF0000" />}
                                    下载次数：{data.downloadCount}
                                  </Typography.Text>
                                    <div>
                                        <Avatar src={data.user?.userAvatar ?? <UserOutlined/>}/>
                                    </div>
                                </Flex>
                            </Card>
                        </Link>
                    </List.Item>
                )}
            />
        </PageContainer>
    );
};

export default IndexPage;
