import {AntDesignOutlined, FireFilled, GithubOutlined} from '@ant-design/icons';
import {DefaultFooter} from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = 'Liang Guo';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`Copyright ${currentYear} | Powered by ${defaultMessage}`}
      links={[
        {
          key: 'author',
          title: (
              <>
                <FireFilled /> Liang Guo
              </>
          ),
          href: 'https://github.com/LiangG0329',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> 源码
            </>
          ),
          href: 'https://github.com/LiangG0329/code-generator',
          blankTarget: true,
        },
          {
              key: 'Ant Design',
              title: (
                  <>
                  <AntDesignOutlined />Ant Design
                  </>
              ),
              href: 'https://ant.design',
              blankTarget: true,
          },
      ]}
    ></DefaultFooter>
  );
};

export default Footer;
