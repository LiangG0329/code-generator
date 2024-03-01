import React, {useEffect, useRef, useState} from 'react';
import {PageContainer, ProFormInstance, ProFormText, ProFormTextArea} from "@ant-design/pro-components";
import {Card, message} from "antd";
import {useModel, useSearchParams} from "@@/exports";
import {editUserUsingPost, getUserVoByIdUsingGet} from "@/services/backend/userController";
import {ProForm, ProFormItem} from "@ant-design/pro-form";
import PictureUploader from "@/components/PictureUploader";
import {history} from '@umijs/max';

const UserUpdatePage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const { initialState, setInitialState } = useModel('@@initialState');
  const id = searchParams.get('id');
  const [oldData, setOldData] = useState<API.UserUpdateRequest>();
  const [avatar, setAvatar] = useState(oldData?.userAvatar);
  const formRef = useRef<ProFormInstance>();

  /**
   * 加载数据
   */
  const loadData = async () => {
    if (!id) {
      return;
    }
    try {
      const res = await getUserVoByIdUsingGet({
        id,
      });
      if (res.data) {
        setOldData(res.data);
      }
    } catch (error: any) {
      message.error('加载数据失败，' + error.message);
    }
  };

  useEffect(() => {
    if (id) {
      loadData();
    }
  }, [id]);

  /**
   * 更新
   * @param values
   */
  const doUpdate = async (values: API.UserEditRequest) => {
    try {
      const res = await editUserUsingPost(values);
      if (res.data) {
        message.success('更新成功');
        if (initialState) {
          setInitialState({
            ...initialState,
            currentUser: {
              ...initialState.currentUser,
              userName:values.userName,
              userProfile:values.userProfile,
              userAvatar:values.userAvatar?values.userAvatar:oldData?.userAvatar,
            }
          })
        }
        history.push('/user/detail');
      }
    } catch (error: any) {
      message.error('更新失败，' + error.message);
    }
  };

  /**
   * 提交
   * @param values
   */
  const doSubmit = async (values: API.UserEditRequest) => {
    if (id) {
      await doUpdate({
        id,
        ...values
      });
    }
  };


  return (
    <PageContainer>
      {(oldData) && (
        <Card>
          <ProForm <API.UserEditRequest>
            formRef={formRef}
            onFinish={async (values) => {
              values.userAvatar = avatar;
              await doSubmit(values);
            }}
          >
            <ProFormText name="userName" label="用户昵称" placeholder="请输入名称" initialValue={oldData.userName}/>
            <ProFormTextArea name="userProfile" label="用户简介" placeholder="请输入描述" initialValue={oldData.userProfile}/>
            <ProFormItem name="userAvatar" label="用户头像" initialValue={oldData.userAvatar}>
                <PictureUploader biz="user_avatar"
                   onChange={(url) => {
                     setAvatar(url);
                   }}
                />
            </ProFormItem>
          </ProForm>
        </Card>
      )}
    </PageContainer>
  )
}

export default UserUpdatePage;
