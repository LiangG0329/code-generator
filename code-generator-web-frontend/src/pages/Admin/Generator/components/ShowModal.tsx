import '@umijs/max';
import {message, Modal} from 'antd';
import React, {useEffect, useState} from 'react';
import beautify from 'json-beautify';

interface Props {
    data?: string;
    visible: boolean;
    onCancel: () => void;
}


/**
 * 更新弹窗
 * @param props
 * @constructor
 */
const ShowModal: React.FC<Props> = (props) => {
    const {data, visible, onCancel} = props;
    const [formattedJsonString, setFormattedJsonString] = useState<string>();

    useEffect(() => {
        // 只在 data 变化时尝试解析 JSON
        if (data) {
            try {
                const jsonData = JSON.parse(data);
                // @ts-ignore
                setFormattedJsonString(beautify(jsonData, null, 2, 80));
            } catch (error: any) {
                message.error("解析 JSON 时发生错误:" +  error.message);
            }
        }
    }, [data]);

    if (!data) {
        return <></>;
    }

    return (
        <Modal
            destroyOnClose
            title={'详细配置信息'}
            open={visible}
            footer={null}
            onCancel={() => {
                onCancel?.();
            }}
        >
            <pre>{formattedJsonString}</pre>
        </Modal>
    );
};

export default ShowModal;
