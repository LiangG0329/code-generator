import React, {useEffect, useState} from 'react';
import {Button, Card, Col, Image, message, Row, Space, Typography,} from 'antd';
import {PageContainer} from "@ant-design/pro-components";
import {Link, useModel} from "@@/exports";
import {getUserVoByIdUsingGet} from "@/services/backend/userController";
import {EditOutlined} from "@ant-design/icons";
import moment from 'moment';

const UserDetailPage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [loading, setLoading] = useState<boolean>(false);
  const [data, setData] = useState<API.User>({});

  /**
   * 加载数据
   */
  const loadData = async () => {
    if (!currentUser) {
      return;
    }
    setLoading(true);
    const id = currentUser.id;
    try {
      const res = await getUserVoByIdUsingGet({
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
  }, [currentUser]);


  return (
    <PageContainer title={<></>} loading={loading}>
      {currentUser && (
        <Card style={{width: '90%'}}>
        <Row justify="space-between" gutter={[40, 40]}>
          <Col flex="auto">
            <Space size="large" align="center">
              <Typography.Title level={3}>用户信息</Typography.Title>
            </Space>
            <div style={{marginBottom: 18}}/>
            <Typography.Text style={{fontSize: '18px'}}>用户昵称：</Typography.Text>
            <Typography.Text style={{fontSize: '18px'}} copyable={true}>{data.userName}</Typography.Text>
            <div style={{marginBottom: 18}}/>
            <Typography.Text style={{fontSize: '18px'}}>用户简介：</Typography.Text>
            <Typography.Text style={{fontSize: '18px'}} copyable={true}>{data.userProfile}</Typography.Text>
            <div style={{marginBottom: 18}}/>
            <Typography.Text
              style={{fontSize: '18px'}}>用户角色：{data.userRole === 'admin' ? '管理员' : '普通用户'}</Typography.Text>
            <div style={{marginBottom: 18}}/>
            <Typography.Text
              style={{fontSize: '18px'}}>创建时间：{moment(data.createTime).format('YYYY-MM-DD hh:mm:ss')}</Typography.Text>
            <div style={{marginBottom: 22}}/>
            <Space size="middle">
              <Link to={`/user/edit?id=${data.id}`}>
                <Button icon={<EditOutlined/>} type={"primary"}>编辑</Button>
              </Link>
            </Space>
          </Col>
          <Col flex="280px">
            <Image src={data.userAvatar} />
          </Col>
        </Row>
      </Card>
      )}
    </PageContainer>
  );
};


export default UserDetailPage;
